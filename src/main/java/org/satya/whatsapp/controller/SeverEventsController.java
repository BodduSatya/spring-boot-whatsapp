package org.satya.whatsapp.controller;

import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.service.FileStorageService;
import org.satya.whatsapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/server-events")
public class SeverEventsController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/sendMsgV4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResponseMessage> sendWAMessageV4(@RequestParam String fileName) throws IOException {
        List<MessageDTO> msgs = fileStorageService.readExcel(fileName);
        return Flux.fromIterable(msgs)
                .map(messageService::sendMessageV3)
                .delayElements(Duration.ofSeconds(5));

        /*List<MessageDTO> msgs = new ArrayList<>();
        MessageDTO msg = new MessageDTO("1","","","","","");
        msgs.add(msg);
        msg = new MessageDTO("2","","","","","");
        msgs.add(msg);
        msg = new MessageDTO("3","","","","","");
        msgs.add(msg);
        msg = new MessageDTO("4","","","","","");
        msgs.add(msg);
        msg = new MessageDTO("5","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("6","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("7","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("8","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("9","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("10","","","","","");
        msgs.add(msg);

        msg = new MessageDTO("11","","","","","");
        msgs.add(msg);

        System.out.println("msgs = " + msgs.size());
        return Flux.fromIterable(msgs)
                .map(messageService::test)
                .delayElements(Duration.ofSeconds(5))
                ;*/

    }

}
