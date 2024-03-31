package org.satya.whatsapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ChatGPTService {
    /*private final WebClient webClient;

    @Value("${openai.api.key}")
    private String api_key;

    @Value("${openai.api.url}")
    private String api_url;

    @Value("${openai.model}")
    private String openai_model;

    public ChatGPTService() {
        System.out.println("api_url = " + api_url);
        this.webClient = WebClient.builder().baseUrl(api_url).build();
    }

    public String getChatResponse(String prompt) {
        System.out.println("api_key = " + api_key);
        System.out.println("prompt = " + prompt);

        StringBuilder requestBody = new StringBuilder("{" +
                "\"model\": \""+openai_model+"\"," +
                "\"messages\":[" +
                    "{\"role\": \"user\", \"content\": \""+prompt+"\"}" +
                "]" +
                "}");

        return webClient.post()
                .uri("/v1/completions")
                .header("Authorization", "Bearer "+api_key)
                .header("content-type", "application/json")
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }*/
}
