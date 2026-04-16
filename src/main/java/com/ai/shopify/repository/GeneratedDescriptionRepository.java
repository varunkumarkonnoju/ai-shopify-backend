package com.ai.shopify.repository;

import com.ai.shopify.entity.GeneratedDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedDescriptionRepository extends JpaRepository<GeneratedDescription, Long> {

    // Existing — used for full history list
    List<GeneratedDescription> findByProductIdOrderByCreatedAtDesc(String productId);

    // NEW — used for smart improvement (Phase 13)
    Optional<GeneratedDescription> findTopByProductIdOrderByCreatedAtDesc(String productId);
}