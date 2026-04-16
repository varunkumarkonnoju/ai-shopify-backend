package com.ai.shopify.exception;

public class InvalidShopTokenException extends RuntimeException {

    private final String shopDomain;

    public InvalidShopTokenException(String shopDomain, String message) {
        super(message);
        this.shopDomain = shopDomain;
    }

    public String getShopDomain() {
        return shopDomain;
    }
}