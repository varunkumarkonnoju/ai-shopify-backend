package com.ai.shopify.service;

import com.ai.shopify.entity.Shop;
import com.ai.shopify.exception.InvalidShopTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class ShopifyService {

    private final WebClient webClient;
    private final ShopService shopService;

    @Value("${shopify.api.version}")
    private String apiVersion;

    public ShopifyService(WebClient.Builder webClientBuilder, ShopService shopService) {
        this.webClient = webClientBuilder.build();
        this.shopService = shopService;
    }

    public String getProducts(String shopDomain) {
        String query = """
                query GetProducts {
                  products(first: 20) {
                    edges {
                      node {
                        id
                        title
                        descriptionHtml
                        handle
                      }
                    }
                  }
                }
                """;

        return callShopify(shopDomain, query, null);
    }

    public String updateProductDescription(String shopDomain, String productId, String description) {
        String mutation = """
                mutation UpdateProductDescription($input: ProductInput!) {
                  productUpdate(input: $input) {
                    product {
                      id
                      title
                      descriptionHtml
                    }
                    userErrors {
                      field
                      message
                    }
                  }
                }
                """;

        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> input = new HashMap<>();
        input.put("id", productId);
        input.put("descriptionHtml", description);
        variables.put("input", input);

        return callShopify(shopDomain, mutation, variables);
    }

    private String callShopify(String shopDomain, String query, Map<String, Object> variables) {
        Shop shop = shopService.getActiveShopOrThrow(shopDomain);

        String endpoint = "https://" + shopDomain + "/admin/api/" + apiVersion + "/graphql.json";

        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);

        if (variables != null) {
            payload.put("variables", variables);
        }

        try {
            return webClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Shopify-Access-Token", shop.getAccessToken())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException.Unauthorized ex) {
            shopService.deactivateShop(shopDomain);
            throw new InvalidShopTokenException(
                    shopDomain,
                    "Shopify access token is invalid or expired. Shop has been deactivated. Reinstall the app for this store."
            );

        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Shopify API error for shop " + shopDomain + ": " + ex.getStatusCode().value() + " " + ex.getResponseBodyAsString()
            );

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected Shopify error for shop " + shopDomain + ": " + ex.getMessage(), ex);
        }
    }
}