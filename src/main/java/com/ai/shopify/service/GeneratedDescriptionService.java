package com.ai.shopify.service;

import com.ai.shopify.dto.GeneratedDescriptionResponse;
import com.ai.shopify.entity.GeneratedDescription;
import com.ai.shopify.repository.GeneratedDescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GeneratedDescriptionService {

    private final GeneratedDescriptionRepository repository;

    public GeneratedDescriptionService(GeneratedDescriptionRepository repository) {
        this.repository = repository;
    }

    public GeneratedDescription save(String productId,
                                     String productTitle,
                                     String prompt,
                                     String rawDescription,
                                     String htmlDescription) {

        GeneratedDescription item = new GeneratedDescription();
        item.setProductId(productId);
        item.setProductTitle(productTitle);
        item.setPrompt(prompt);
        item.setRawDescription(rawDescription);
        item.setHtmlDescription(htmlDescription);
        item.setCreatedAt(LocalDateTime.now());

        return repository.save(item);
    }

    public List<GeneratedDescriptionResponse> getHistoryByProductId(String productId) {
        return repository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 🔥 NEW METHOD — Phase 13 (core upgrade)
    public Optional<GeneratedDescriptionResponse> getLatestByProductId(String productId) {
        return repository.findTopByProductIdOrderByCreatedAtDesc(productId)
                .map(this::toResponse);
    }

    private GeneratedDescriptionResponse toResponse(GeneratedDescription entity) {
        GeneratedDescriptionResponse response = new GeneratedDescriptionResponse();
        response.setId(entity.getId());
        response.setProductId(entity.getProductId());
        response.setProductTitle(entity.getProductTitle());
        response.setPrompt(entity.getPrompt());
        response.setRawDescription(entity.getRawDescription());
        response.setHtmlDescription(entity.getHtmlDescription());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}