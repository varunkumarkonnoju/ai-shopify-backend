package com.ai.shopify.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CustomAppController {

    @Value("${shopify.custom.install-url}")
    private String customInstallUrl;

    @Value("${shopify.store.domain}")
    private String storeDomain;

    @GetMapping("/api/custom-app/install-link")
    public Map<String, String> getInstallLink() {
        return Map.of(
                "installUrl", customInstallUrl,
                "shopDomain", storeDomain
        );
    }
}