package com.ai.shopify.service;

import com.ai.shopify.entity.Shop;
import com.ai.shopify.repository.ShopRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class OAuthService {

    @Value("${shopify.api.key}")
    private String apiKey;

    @Value("${shopify.api.secret}")
    private String apiSecret;

    @Value("${shopify.redirect.uri}")
    private String redirectUri;

    @Value("${shopify.scopes}")
    private String scopes;

    private final WebClient webClient;
    private final ShopRepository shopRepository;

    public OAuthService(WebClient webClient, ShopRepository shopRepository) {
        this.webClient = webClient;
        this.shopRepository = shopRepository;
    }

    public String buildInstallUrl(String shop) {
        return "https://" + shop + "/admin/oauth/authorize"
                + "?client_id=" + apiKey
                + "&scope=" + scopes
                + "&redirect_uri=" + redirectUri;
    }

    public String exchangeCodeForToken(String shop, String code) {
        Map<String, String> requestBody = Map.of(
                "client_id", apiKey,
                "client_secret", apiSecret,
                "code", code
        );

        JsonNode response = webClient.post()
                .uri("https://" + shop + "/admin/oauth/access_token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("Failed to get access token from Shopify");
        }

        String accessToken = response.get("access_token").asText();

        Shop savedShop = shopRepository.findByShopDomain(shop)
                .orElseGet(Shop::new);

        savedShop.setShopDomain(shop);
        savedShop.setAccessToken(accessToken);

        shopRepository.save(savedShop);

        return accessToken;
    }
}