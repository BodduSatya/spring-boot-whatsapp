package org.satya.whatsapp.controller;

import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WspController {

    @Autowired
    private MessageService messageService;

    @RequestMapping("/sendMsg")
    public ResponseMessage sendWAMessage(@RequestBody MessageDTO msg) {
        return messageService.sendMessageV2(msg);
    }

    @RequestMapping("/sendMsgV3")
    public ResponseMessage sendWAMessageV3(@RequestBody MessageDTO msg) {
        return messageService.sendMessageV3(msg);
    }

}
