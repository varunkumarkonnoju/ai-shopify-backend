package com.ai.shopify.controller;

import com.ai.shopify.dto.GeneratedDescriptionResponse;
import com.ai.shopify.service.GeneratedDescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shopify")
public class HistoryController {

    private final GeneratedDescriptionService generatedDescriptionService;

    public HistoryController(GeneratedDescriptionService generatedDescriptionService) {
        this.generatedDescriptionService = generatedDescriptionService;
    }

    @GetMapping("/history")
    public List<GeneratedDescriptionResponse> getHistory(@RequestParam String productId) {
        return generatedDescriptionService.getHistoryByProductId(productId);
    }

    @GetMapping("/history/latest")
    public ResponseEntity<?> getLatestHistory(@RequestParam String productId) {
        return generatedDescriptionService.getLatestByProductId(productId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().body(null));
    }
}