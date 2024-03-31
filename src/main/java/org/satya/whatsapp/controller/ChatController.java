package org.satya.whatsapp.controller;

import org.satya.whatsapp.service.ChatGPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class ChatController {

    private final ChatGPTService chatGPTService;

    @Autowired
    public ChatController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }
    /*@PostMapping("/chat")
    public String chatWithGPT(@RequestBody String userInput) {
        return chatGPTService.getChatResponse(userInput);
    }*/

}
