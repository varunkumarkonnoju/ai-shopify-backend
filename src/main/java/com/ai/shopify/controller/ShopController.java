package com.ai.shopify.controller;

import com.ai.shopify.dto.ShopResponse;
import com.ai.shopify.entity.Shop;
import com.ai.shopify.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/api/shops")
    public List<ShopResponse> getAllShops() {
        return shopService.getAllActiveShops()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ShopResponse toResponse(Shop shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setShopDomain(shop.getShopDomain());
        response.setInstalledAt(shop.getInstalledAt());
        response.setActive(shop.isActive());
        return response;
    }
}