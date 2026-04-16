package com.ai.shopify.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CustomInstallLinkController {

    @Value("${shopify.custom.install-url:}")
    private String customInstallUrl;

    @GetMapping("/api/shopify/install-link")
    public Map<String, String> getInstallLink(@RequestParam String shop) {
        if (customInstallUrl == null || customInstallUrl.isBlank()) {
            throw new RuntimeException("shopify.custom.install-url is missing");
        }

        return Map.of("url", customInstallUrl);
    }
}