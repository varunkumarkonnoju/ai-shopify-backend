package com.ai.shopify.controller;

import com.ai.shopify.entity.Shop;
import com.ai.shopify.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/shopify")
public class ShopifyAuthController {

    @Value("${shopify.api.key}")
    private String apiKey;

    @Value("${shopify.api.secret}")
    private String apiSecret;

    @Value("${shopify.app.url}")
    private String appUrl;

    @Value("${shopify.redirect.uri}")
    private String redirectUri;

    @Value("${shopify.scopes}")
    private String scopes;

    @Value("${frontend.app.url}")
    private String frontendAppUrl;

    private final ShopRepository shopRepository;

    public ShopifyAuthController(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @GetMapping("/install")
    public ResponseEntity<Void> install(@RequestParam String shop) {
        validateShopDomain(shop);

        String fullRedirectUri = appUrl + redirectUri;

        String installUrl =
                "https://" + shop +
                        "/admin/oauth/authorize" +
                        "?client_id=" + url(apiKey) +
                        "&scope=" + url(scopes) +
                        "&redirect_uri=" + url(fullRedirectUri);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(installUrl))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam MultiValueMap<String, String> queryParams) {
        String shop = queryParams.getFirst("shop");
        String code = queryParams.getFirst("code");
        String hmac = queryParams.getFirst("hmac");

        validateShopDomain(shop);

        if (code == null || code.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (hmac == null || hmac.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!isValidHmac(queryParams, hmac)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String tokenUrl = "https://" + shop + "/admin/oauth/access_token";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("client_id", apiKey);
        body.put("client_secret", apiSecret);
        body.put("code", code);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, body, Map.class);

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String accessToken = (String) response.getBody().get("access_token");

        Shop shopEntity = shopRepository.findByShopDomain(shop).orElseGet(Shop::new);
        shopEntity.setShopDomain(shop);
        shopEntity.setAccessToken(accessToken);
        shopEntity.setInstalledAt(LocalDateTime.now());
        shopEntity.setActive(true);

        shopRepository.save(shopEntity);

        String frontendRedirect =
                frontendAppUrl + "?shop=" + url(shop) + "&installed=true";

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendRedirect))
                .build();
    }

    private void validateShopDomain(String shop) {
        if (shop == null || shop.isBlank() || !shop.endsWith(".myshopify.com")) {
            throw new IllegalArgumentException("Invalid Shopify shop domain: " + shop);
        }
    }

    private boolean isValidHmac(MultiValueMap<String, String> queryParams, String providedHmac) {
        try {
            Map<String, String> sorted = new TreeMap<>();

            for (Map.Entry<String, java.util.List<String>> entry : queryParams.entrySet()) {
                String key = entry.getKey();

                if ("hmac".equals(key) || "signature".equals(key)) {
                    continue;
                }

                String value = entry.getValue() != null && !entry.getValue().isEmpty()
                        ? entry.getValue().get(0)
                        : "";

                sorted.put(key, value);
            }

            StringBuilder message = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                if (!first) {
                    message.append("&");
                }
                message.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(message.toString().getBytes(StandardCharsets.UTF_8));
            String calculatedHmac = bytesToHex(hash);

            return constantTimeEquals(calculatedHmac, providedHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}