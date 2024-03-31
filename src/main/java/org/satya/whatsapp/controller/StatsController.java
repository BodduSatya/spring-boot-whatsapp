package org.satya.whatsapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private MessageService messageService;

//    @GetMapping("/sendMsg")
//    public String hello(MessageDTO message, Model model) {
//        model.addAttribute("message", message);
//        return "SendMsg";
//    }

    @GetMapping("/getAllMessages")
    public List<MessageDTO> getAllMessagesBetweenDates(@RequestParam String fromDate,
                                                       @RequestParam String toDate,
                                                       @RequestParam String mobileNo ){
        return messageService.getAllMessagesBetweenDates(fromDate,toDate,mobileNo);
    }

    @GetMapping("/sendQueuedMessages")
    public ResponseEntity<?> sendQueuedMessagesBetweenDates(@RequestParam String fromDate,
                                                            @RequestParam String toDate,
                                                            @RequestParam String mobileNo ){
        List<MessageDTO> messages = messageService.getAllNonSendMessagesBetweenDates(fromDate, toDate, mobileNo);
        List<MessageDTO> queuedMessages = messages.stream()
                .filter(m -> "0".equalsIgnoreCase(m.getSendStatus()))
                .toList();

        final long[] success_count = {0};
        final long[] failure_count = {0};
        if(!queuedMessages.isEmpty()){
            log.info("$> Queued Messages Count {} ",queuedMessages.size());
            queuedMessages.forEach(m->{
                messageService.sendMessageV2(m);
                if( "1".equalsIgnoreCase(m.getSendStatus()) )
                    success_count[0]++;
                else
                    failure_count[0]++;
            });
        } else{
            log.info("$> No Queued Messages ");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("total_messages", queuedMessages.size());
        response.put("success_count", success_count[0]);
        response.put("failure_count", failure_count[0]);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}