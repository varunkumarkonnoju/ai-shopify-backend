package com.ai.shopify.repository;

import com.ai.shopify.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByShopDomain(String shopDomain);
    Optional<Shop> findByShopDomainAndActiveTrue(String shopDomain);
}