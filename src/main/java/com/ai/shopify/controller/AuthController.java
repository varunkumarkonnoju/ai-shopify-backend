package com.ai.shopify.controller;

import com.ai.shopify.service.OAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OAuthService oauthService;

    public AuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/install")
    public Map<String, String> install(@RequestParam String shop) {
        return Map.of("installUrl", oauthService.buildInstallUrl(shop));
    }

    @GetMapping("/callback")
    public Map<String, String> callback(
            @RequestParam String shop,
            @RequestParam String code
    ) {
        String accessToken = oauthService.exchangeCodeForToken(shop, code);

        return Map.of(
                "message", "Shop installed successfully",
                "shop", shop,
                "accessToken", accessToken
        );
    }
}