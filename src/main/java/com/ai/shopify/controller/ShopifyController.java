package com.ai.shopify.controller;

import com.ai.shopify.service.ShopifyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopify")
public class ShopifyController {

    private final ShopifyService shopifyService;

    public ShopifyController(ShopifyService shopifyService) {
        this.shopifyService = shopifyService;
    }

    @GetMapping("/products")
    public String getProducts(@RequestParam String shop) {
        return shopifyService.getProducts(shop);
    }

    @PutMapping("/products/description")
    public String updateDescription(@RequestBody UpdateRequest request) {
        return shopifyService.updateProductDescription(
                request.getShopDomain(),
                request.getProductId(),
                request.getDescription()
        );
    }

    @PostMapping("/generate-and-update")
    public String generateAndUpdate(@RequestBody GenerateRequest request) {
        throw new RuntimeException("Implement generateAndUpdate logic here or wire it to your existing AI flow");
    }

    public static class UpdateRequest {
        private String shopDomain;
        private String productId;
        private String description;

        public String getShopDomain() {
            return shopDomain;
        }

        public void setShopDomain(String shopDomain) {
            this.shopDomain = shopDomain;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class GenerateRequest {
        private String shopDomain;
        private String productId;
        private String prompt;

        public String getShopDomain() {
            return shopDomain;
        }

        public void setShopDomain(String shopDomain) {
            this.shopDomain = shopDomain;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}