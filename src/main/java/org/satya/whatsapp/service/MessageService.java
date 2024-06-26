package org.satya.whatsapp.service;

import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.contact.ContactJid;
import it.auties.whatsapp.model.message.standard.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.satya.whatsapp.entity.Message;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.repository.MessageRepository;
import org.satya.whatsapp.utils.MediaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageService {

    @Autowired
    private Whatsapp whatsappApi;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageRepository messageRepository;

    public boolean saveMessage(Message message){
        try {
            messageRepository.save(message);
        }catch (Exception e){
            System.out.println("saveMessage e = " + e);
        }
        return true;
    }

    public boolean updateMessageStatus(Message message){
        try {
            Optional<Message> savedMessage = messageRepository.findById(message.getId());
            if(savedMessage.isPresent()){
                savedMessage.get().setSendStatus(message.getSendStatus());
                savedMessage.get().setSenton(LocalDateTime.now());
                messageRepository.save(savedMessage.get());
                return true;
            }
        } catch (Exception e) {
            System.out.println("updateMessageStatus e = " + e);
        }
        return false;
    }

    public List<MessageDTO> getAllMessagesBetweenDates(String fromDate, String toDate, String mobileNo ) {

        fromDate = fromDate.contains(":")? fromDate : fromDate+" 00:00";
        toDate = toDate.contains(":")? toDate : toDate+" 23:59";

        LocalDateTime from = LocalDateTime.parse(fromDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        from = LocalDateTime.parse(from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        LocalDateTime to = LocalDateTime.parse(toDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        to = LocalDateTime.parse(to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        log.info("From Date - {} To Date - {} ",from,to);

        List<Message> messages = null;
        if( mobileNo!=null && !mobileNo.trim().isEmpty()){
            messages = messageRepository.getAllMessagesBetweenDatesMobNo(from,to,mobileNo);
        }
        else{
            messages = messageRepository.getAllMessagesBetweenDates(from,to);
        }
        return messages.stream()
                .map((message)->modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getAllNonSendMessagesBetweenDates(String fromDate, String toDate, String mobileNo ) {

        fromDate = fromDate.contains(":")? fromDate : fromDate+" 00:00";
        toDate = toDate.contains(":")? toDate : toDate+" 23:59";

        LocalDateTime from = LocalDateTime.parse(fromDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        from = LocalDateTime.parse(from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        LocalDateTime to = LocalDateTime.parse(toDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        to = LocalDateTime.parse(to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        log.info("From Date - {} To Date - {} ",from,to);

        List<Message> messages = null;
        if( mobileNo!=null && !mobileNo.trim().isEmpty()){
            messages = messageRepository.getAllNonSendMessagesBetweenDatesMobNo(from,to,mobileNo);
        }
        else{
            messages = messageRepository.getAllNonSendMessagesBetweenDates(from,to);
        }

        return messages.stream()
                .map((message)->modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getNonSendMessagesByMIDs(String[] mids ) {

        log.info("MIDS - {}  ",mids);

        List<Message> messages = null;
        if( mids!=null && mids.length >0 ){
            messages = messageRepository.getNonSendMessagesByMIDs(mids);
        }

        return messages.stream()
                .map((message)->modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public synchronized ResponseMessage sendMessageV2(MessageDTO msg){
        org.satya.whatsapp.entity.Message message = null;
        try {
            message = new org.satya.whatsapp.entity.Message(msg.getToMobileNumber(),msg.getMessage(),msg.getTypeOfMsg(),
                    LocalDateTime.now(),msg.getMediaUrl(),msg.getCaption(),msg.getId());

            if( msg.getId() == 0 )
                saveMessage(message);

            Chat chat = null;
            //ContactJid contactJid = null;
            ContactJid contactJid = ContactJid.of(msg.getToMobileNumber());
            /*if(msg.isGroupMsg()) {
                chat = whatsappApi.store()
                        .findChatByJid(ContactJid.of(msg.getToMobileNumber()))
                        .filter(Chat::isGroup)
                        .orElseThrow(() -> {
                                    message.setSendStatus("2");
                                    updateMessageStatus(message);
                                    return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                                }
                        );
            }
            else{
                contactJid = ContactJid.builder().server(ContactJid.Server.WHATSAPP).user(msg.getToMobileNumber()).build();
                *//*chat = whatsappApi.store()
                        .findChatByJid(contactJid)
                        .orElseThrow(() -> {
                                    message.setSendStatus("2");
                                    updateMessageStatus(message);
                                    return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                                }
                        );*//*

            }*/

            //text / image / audio  / video / gif / document / reaction / remove_reaction
            switch (msg.getTypeOfMsg() ) {
                case "text" -> {
                    if( msg.getMessage()!=null && !msg.getMessage().isEmpty())
                        whatsappApi.sendMessage(contactJid, msg.getMessage());
                    else
                        log.info(" Missing required parameter message");
                }
                case "image" -> {
                    if( msg.getMediaUrl()!=null && !msg.getMediaUrl().isEmpty()) {
                        if( msg.isLogRequired()) log.info("Sending image...");
                        var image = ImageMessage.simpleBuilder()
                                .media(MediaUtils.readBytes(msg.getMediaUrl()))
                                .caption(msg.getCaption())
                                .build();
                        whatsappApi.sendMessage(contactJid, image).join();
                        if( msg.isLogRequired()) log.info("Sent image");
                    }
                    else
                        log.info(" Missing required parameter mediaUrl ");
                }
                case "document" -> {
                    if( msg.getMediaUrl()!=null && !msg.getMediaUrl().isEmpty()) {
                        if( msg.isLogRequired()) log.info("Sending document...");
                        var document = DocumentMessage.simpleBuilder()
                                .media(MediaUtils.readBytes(msg.getMediaUrl()))
                                .title(msg.getCaption())
                                .fileName(msg.getFileName())
//                              .pageCount(1)
                                .build();
                        whatsappApi.sendMessage(contactJid, document).join();
                        if( msg.isLogRequired()) log.info("Sent document");
                    }
                    else
                        log.info(" Missing required parameter mediaUrl ");
                }
                case "audio" -> {
                    if( msg.getMediaUrl()!=null && !msg.getMediaUrl().isEmpty()) {
                        if( msg.isLogRequired()) log.info("Sending audio...");
                        var audio = AudioMessage.simpleBuilder()
                                .media(MediaUtils.readBytes(msg.getMediaUrl()))
                                .voiceMessage(true)
                                .build();
                        whatsappApi.sendMessage(contactJid, audio).join();
                        if( msg.isLogRequired()) log.info("Sent audio");
                    }
                    else
                        log.info(" Missing required parameter mediaUrl ");
                }
                case "video" -> {
                    if( msg.getMediaUrl()!=null && !msg.getMediaUrl().isEmpty()) {
                        if( msg.isLogRequired()) log.info("Sending video...");
                        var video = VideoMessage.simpleVideoBuilder()
                                .media(MediaUtils.readBytes(msg.getMediaUrl()))
                                .caption(msg.getCaption()).build();
                        whatsappApi.sendMessage(contactJid, video).join();
                        if( msg.isLogRequired()) log.info("Sent video");
                    }
                    else
                        log.info(" Missing required parameter mediaUrl ");
                }
                case "gif" -> {
                    if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
                        if( msg.isLogRequired()) log.info("Sending gif...");
                        var video = VideoMessage.simpleGifBuilder()
                                .media(MediaUtils.readBytes(msg.getMediaUrl()))
                                .caption(msg.getCaption()).build();
                        whatsappApi.sendMessage(contactJid, video).join();
                        if( msg.isLogRequired()) log.info("Sent video");
                    } else
                        log.info(" Missing required parameter mediaUrl ");
                }
                default -> {
                    System.out.println("No Handler found for message type = " + msg.getTypeOfMsg());
                }
            }

            message.setSendStatus("1");
            updateMessageStatus(message);

        } catch (Exception e) {
//            message.setSendStatus("2");
//            updateMessageStatus(message);
            System.out.println("e = " + e.getMessage());
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Failed! message sending failed to "+msg.getToMobileNumber() );
        }
        return new ResponseMessage(HttpStatus.OK,"Message Sent to "+msg.getToMobileNumber() );
    }

    public synchronized ResponseMessage sendMessageV3(MessageDTO msg){
        org.satya.whatsapp.entity.Message message = null;
        try {
            String mobileNos[] = msg.getToMobileNumber().split(",");

            for( int i = 0; i < mobileNos.length; i++ ) {

                msg.setToMobileNumber(mobileNos[i]);

                message = new org.satya.whatsapp.entity.Message(msg.getToMobileNumber(), msg.getMessage(), msg.getTypeOfMsg(),
                        LocalDateTime.now(),msg.getMediaUrl(),msg.getCaption(),msg.getId());

                saveMessage(message);

//                Chat chat = null;
                ContactJid contactJid = ContactJid.of(msg.getToMobileNumber());
                /*if (msg.isGroupMsg()) {
                    contactJid = ContactJid.of(msg.getToMobileNumber());
                    *//*chat = whatsappApi.store()
                            .findChatByJid(ContactJid.of(msg.getToMobileNumber()))
                            .filter(Chat::isGroup)
                            .orElseThrow(() -> {
                                        message.setSendStatus("2");
                                        updateMessageStatus(message);
                                        return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                                    }
                            );*//*
                } else {
                     contactJid = ContactJid.builder().server(ContactJid.Server.WHATSAPP).user(msg.getToMobileNumber()).build();
//                    chat = whatsappApi.store()
//                            .findChatByJid(contactJid)
//                            .orElseThrow(() -> {
//                                        message.setSendStatus("2");
//                                        updateMessageStatus(message);
//                                        return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
//                                    }
//                            );
                }*/

                //text / image / audio  / video / gif / document / reaction / remove_reaction
                switch (msg.getTypeOfMsg()) {
                    case "text" -> {
                        if (msg.getMessage() != null && !msg.getMessage().isEmpty())
                            whatsappApi.sendMessage(contactJid, msg.getMessage());
                        else
                            log.info(" Missing required parameter message");
                    }
                    case "image" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending image...");
                                var image = ImageMessage.simpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .caption(k == 0 ? msg.getCaption() : "")
                                        .build();
                                whatsappApi.sendMessage(contactJid, image).join();
                                if (msg.isLogRequired()) log.info("Sent image");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "document" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending document...");
                                var document = DocumentMessage.simpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .title(k == 0 ? msg.getCaption() : "")
                                        .fileName((k + 1) + msg.getFileName2()[k].substring(msg.getFileName2()[k].lastIndexOf(".")))
//                              .pageCount(1)
                                        .build();
                                whatsappApi.sendMessage(contactJid, document).join();
                                if (msg.isLogRequired()) log.info("Sent document");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "audio" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending audio...");
                                var audio = AudioMessage.simpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .voiceMessage(true)
                                        .build();
                                whatsappApi.sendMessage(contactJid, audio).join();
                                if (msg.isLogRequired()) log.info("Sent audio");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "video" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending video...");
                                var video = VideoMessage.simpleVideoBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .caption(msg.getCaption()).build();
                                whatsappApi.sendMessage(contactJid, video).join();
                                if (msg.isLogRequired()) log.info("Sent video");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "gif" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending gif...");
                                var video = VideoMessage.simpleGifBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .caption(msg.getCaption()).build();
                                whatsappApi.sendMessage(contactJid, video).join();
                                if (msg.isLogRequired()) log.info("Sent video");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }

                    default -> {
                        System.out.println("No Handler found for message type = " + msg.getTypeOfMsg());
                    }
                }

                message.setSendStatus("1");
                updateMessageStatus(message);

                try {
                    // Sleep for 1 second (1000 milliseconds)
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // Handle interrupted exception
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            if( message!=null ) {
                message.setSendStatus("2");
                updateMessageStatus(message);
            }
            System.out.println("e = " + e.getMessage());
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error!" );
        }
        return new ResponseMessage(HttpStatus.OK,"Message Sent." );
    }

    public synchronized ResponseMessage test(MessageDTO msg)  {

        System.out.println("service test() start"+msg.getToMobileNumber() );
        try {
            Thread.sleep(Duration.ofSeconds(3));
        }catch (Exception e){
            System.out.println("e = " + e);
        }
        System.out.println("service test() end"+msg.getToMobileNumber() );
        return new ResponseMessage(HttpStatus.OK,"Message Sent."+msg.getToMobileNumber() );
    }

}
