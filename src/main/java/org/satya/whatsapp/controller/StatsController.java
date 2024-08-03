package org.satya.whatsapp.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/stats")
public class StatsController {

    private static final Logger log = LogManager.getLogger(StatsController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping("/getAllMessages")
    public List<MessageDTO> getAllMessagesBetweenDates(@RequestParam String fromDate,
                                                       @RequestParam String toDate,
                                                       @RequestParam String mobileNo,
                                                       @RequestParam String msgStatus){
        return messageService.getAllMessagesBetweenDates(fromDate,toDate,mobileNo,msgStatus);
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
                messageService.sendMessageV3(m);
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


    @PostMapping("/sendQueuedMessagesByMID")
    public ResponseEntity<?> sendQueuedMessagesByMID(@RequestBody String[] mids){

//        System.out.println("mids = " + Arrays.toString(mids));
        List<MessageDTO> messages = messageService.getNonSendMessagesByMIDs(mids);
        List<MessageDTO> queuedMessages = messages.stream()
                .filter(m -> !"1".equalsIgnoreCase(m.getSendStatus()))
                .toList();

        final long[] success_count = {0};
        final long[] failure_count = {0};
        if(!queuedMessages.isEmpty()){
            log.info("$> Queued Messages Count {} ",queuedMessages.size());
            queuedMessages.forEach(m->{
                ResponseMessage responseMessage = messageService.sendMessageV3(m);
                if( responseMessage.getStatus() == HttpStatus.OK )
                    success_count[0]++;
                else
                    failure_count[0]++;

                try {
                    Thread.sleep(Duration.ofSeconds(10));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

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
