package com.ai.shopify.config;

import com.ai.shopify.entity.Shop;
import com.ai.shopify.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class ShopBootstrapConfig {

    @Value("${shopify.store.domain:}")
    private String storeDomain;

    @Value("${shopify.admin.access-token:}")
    private String adminAccessToken;

    @Bean
    public ApplicationRunner bootstrapShop(ShopRepository shopRepository) {
        return args -> {
            if (storeDomain == null || storeDomain.isBlank()) {
                return;
            }

            if (adminAccessToken == null || adminAccessToken.isBlank()) {
                return;
            }

            Shop shop = shopRepository.findByShopDomain(storeDomain).orElseGet(Shop::new);
            shop.setShopDomain(storeDomain);
            shop.setAccessToken(adminAccessToken);
            shop.setActive(true);

            if (shop.getInstalledAt() == null) {
                shop.setInstalledAt(LocalDateTime.now());
            }

            shopRepository.save(shop);
        };
    }
}