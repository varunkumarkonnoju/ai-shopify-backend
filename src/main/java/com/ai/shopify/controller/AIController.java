package com.ai.shopify.controller;

import com.ai.shopify.dto.GenerateRequest;
import com.ai.shopify.service.OpenAIService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final OpenAIService openAIService;

    public AIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/generate")
    public Map<String, String> generate(@RequestBody GenerateRequest request) {
        String result = openAIService.generateDescription(request.getPrompt());
        return Map.of("description", result);
    }
}