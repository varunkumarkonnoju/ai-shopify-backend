package com.ai.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAIService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateDescription(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "temperature", 1.0,
                "presence_penalty", 0.8,
                "frequency_penalty", 0.6,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        You are a high-performance ecommerce copywriter for Shopify brands.

                                        Your job:
                                        - write persuasive, original product descriptions
                                        - avoid sounding generic, robotic, or repetitive
                                        - avoid copying the same structure repeatedly
                                        - use fresh wording, varied sentence patterns, and strong hooks
                                        - focus on benefits, emotional appeal, and conversion
                                        - output only the product description
                                        - do not include explanations, notes, or commentary
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        JsonNode response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null
                || response.get("choices") == null
                || response.get("choices").isEmpty()) {
            throw new RuntimeException("Invalid response from OpenAI");
        }

        return response.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
    }
}