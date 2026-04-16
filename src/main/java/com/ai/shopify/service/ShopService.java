package com.ai.shopify.service;

import com.ai.shopify.dto.RegisterShopRequest;
import com.ai.shopify.entity.Shop;
import com.ai.shopify.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop registerOrUpdate(RegisterShopRequest request) {
        Shop shop = shopRepository.findByShopDomain(request.getShopDomain())
                .orElseGet(Shop::new);

        shop.setShopDomain(request.getShopDomain());
        shop.setAccessToken(request.getAccessToken());
        shop.setInstalledAt(LocalDateTime.now());
        shop.setActive(true);

        return shopRepository.save(shop);
    }

    public Shop getActiveShopOrThrow(String shopDomain) {
        return shopRepository.findByShopDomainAndActiveTrue(shopDomain)
                .orElseThrow(() -> new RuntimeException("Active shop not found for domain: " + shopDomain));
    }

    public List<Shop> getAllActiveShops() {
        return shopRepository.findAll()
                .stream()
                .filter(Shop::isActive)
                .toList();
    }

    public void deactivateShop(String shopDomain) {
        shopRepository.findByShopDomain(shopDomain).ifPresent(shop -> {
            shop.setActive(false);
            shopRepository.save(shop);
        });
    }

    public void reactivateShop(String shopDomain, String accessToken) {
        Shop shop = shopRepository.findByShopDomain(shopDomain).orElseGet(Shop::new);
        shop.setShopDomain(shopDomain);
        shop.setAccessToken(accessToken);
        shop.setActive(true);

        if (shop.getInstalledAt() == null) {
            shop.setInstalledAt(LocalDateTime.now());
        }

        shopRepository.save(shop);
    }
}