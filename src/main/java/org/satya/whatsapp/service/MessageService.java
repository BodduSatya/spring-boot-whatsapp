package org.satya.whatsapp.service;

import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.model.contact.ContactJid;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.standard.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.satya.whatsapp.entity.Message;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.repository.MessageCriteriaRepository;
import org.satya.whatsapp.repository.MessageRepository;
import org.satya.whatsapp.utils.MediaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    @Autowired
    private MessageCriteriaRepository messageCriteriaRepository;

    public void saveMessage(Message message){
        try {
            messageRepository.save(message);
        }catch (Exception e){
            System.out.println("saveMessage e = " + e);
        }
    }

    public void updateMessageStatus(Message message){
        try {
            Optional<Message> savedMessage = messageRepository.findById(message.getId());
            if(savedMessage.isPresent()){
                savedMessage.get().setSendStatus(message.getSendStatus());
                savedMessage.get().setSenton(LocalDateTime.now());
                messageRepository.save(savedMessage.get());
            }
        } catch (Exception e) {
            System.out.println("updateMessageStatus e = " + e);
        }
    }

    public List<MessageDTO> getAllMessagesBetweenDates(String fromDate, String toDate, String mobileNo,String msgStatus ) {

        fromDate = fromDate.contains(":")? fromDate : fromDate+" 00:00";
        toDate = toDate.contains(":")? toDate : toDate+" 23:59";

        LocalDateTime from = LocalDateTime.parse(fromDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        from = LocalDateTime.parse(from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        LocalDateTime to = LocalDateTime.parse(toDate,DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        to = LocalDateTime.parse(to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        log.info("From Date - {} To Date - {} ",from,to);

        List<Message> messages = null;
//        if( mobileNo!=null && !mobileNo.trim().isEmpty()){
//            messages = messageRepository.getAllMessagesBetweenDatesMobNo(from,to,mobileNo);
//        }
//        else{
//            messages = messageRepository.getAllMessagesBetweenDates(from,to);
//        }
        messages = messageCriteriaRepository.getMessagesBetweenDates(from,to,mobileNo,msgStatus);

//        return messages.stream()
//                .map((message)->modelMapper.map(message, MessageDTO.class))
//                .collect(Collectors.toList());

        return messages.stream()
                .map(message -> new MessageDTO(
                        message.getToMobileNumber(),
                        message.getTypeOfMsg(),
                        message.getMessage(),
                        message.getMediaUrl(),
                        message.getCaption(),
                        message.getFileName(),
                        message.getCreatedon(),
                        message.getSenton(),
                        message.getId(),
                        message.getSendStatus()))
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

    /*public synchronized ResponseMessage sendMessageV2(MessageDTO msg){
        org.satya.whatsapp.entity.Message message = null;
        try {
            message = new org.satya.whatsapp.entity.Message(msg.getToMobileNumber(),msg.getMessage(),msg.getTypeOfMsg(),
                    LocalDateTime.now(),msg.getMediaUrl(),msg.getCaption(),msg.getId());

            if( msg.getId() == 0 )
                saveMessage(message);

            Chat chat = null;
            //ContactJid contactJid = null;
            ContactJid contactJid = ContactJid.of(msg.getToMobileNumber());
            *//*if(msg.isGroupMsg()) {
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
                *//**//*chat = whatsappApi.store()
                        .findChatByJid(contactJid)
                        .orElseThrow(() -> {
                                    message.setSendStatus("2");
                                    updateMessageStatus(message);
                                    return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                                }
                        );*//**//*

            }*//*

            CompletableFuture<MessageInfo> mi = null;
            //text / image / audio  / video / gif / document / reaction / remove_reaction
            switch (msg.getTypeOfMsg() ) {
                case "text" -> {
                    if( msg.getMessage()!=null && !msg.getMessage().isEmpty()) {
                        mi = whatsappApi.sendMessage(contactJid, msg.getMessage());
                        System.out.println("mi = " + mi.join());
                    }
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
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Message Send failed to "+msg.getToMobileNumber() );
        }
        return new ResponseMessage(HttpStatus.OK,"Message Sent to "+msg.getToMobileNumber() );
    }*/

    public synchronized ResponseMessage sendMessageV3(MessageDTO msg){
        org.satya.whatsapp.entity.Message message = null;
        String[] mobileNos = null;
        try {
            mobileNos = msg.getToMobileNumber().split(",");

            for (String mobileNo : mobileNos) {

                msg.setToMobileNumber(mobileNo);

                if ( !"text".equalsIgnoreCase(msg.getTypeOfMsg())
                        && msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0 ) {
                    for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                        message = new Message(msg.getToMobileNumber(), msg.getMessage(), msg.getTypeOfMsg(),
                                LocalDateTime.now(), msg.getMediaUrl2()[k], k ==0 ? msg.getCaption() : "", msg.getId());

                        if( msg.getId() == 0 )
                            saveMessage(message);
                    }
                }
                else{
                    message = new Message(msg.getToMobileNumber(), msg.getMessage(), msg.getTypeOfMsg(),
                            LocalDateTime.now(), msg.getMediaUrl(), msg.getCaption(), msg.getId());

                    if( msg.getId() == 0 )
                        saveMessage(message);
                }

                ContactJid contactJid = ContactJid.of(msg.getToMobileNumber());

                //text / image / audio  / video / gif / document / reaction / remove_reaction
                Message finalMessage = message;
                switch (msg.getTypeOfMsg()) {
                    case "text" -> {
                        if (msg.getMessage() != null && !msg.getMessage().isEmpty()) {
                            CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, msg.getMessage());
                            msgFuture.thenAccept(result -> {
                                finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                updateMessageStatus(finalMessage);
                            });
                        } else
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
                                CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, image);
                                msgFuture.thenAccept(result -> {
                                    finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                    updateMessageStatus(finalMessage);
                                });
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
                                CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, document);
                                System.out.println("msgFuture = " + msgFuture);
                                msgFuture.thenAccept(result -> {
                                    System.out.println("result = " + result);
                                    finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                    updateMessageStatus(finalMessage);
                                });
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
                                CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, audio);
                                msgFuture.thenAccept(result -> {
                                    finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                    updateMessageStatus(finalMessage);
                                });
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
                                CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, video);
                                msgFuture.thenAccept(result -> {
                                    finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                    updateMessageStatus(finalMessage);
                                });
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
                                CompletableFuture<MessageInfo> msgFuture = whatsappApi.sendMessage(contactJid, video);
                                msgFuture.thenAccept(result -> {
                                    finalMessage.setSendStatus("SERVER_ACK".equalsIgnoreCase(result.status().name()) ? "1" : "2");
                                    updateMessageStatus(finalMessage);
                                });
                                if (msg.isLogRequired()) log.info("Sent video");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }

                    default -> {
                        System.out.println("No Handler found for message type = " + msg.getTypeOfMsg());
                    }
                }

                try {
                    // Sleep for 1 second (1000 milliseconds)
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Handle interrupted exception
                    e.printStackTrace();
                }

//                System.out.println("finalMessage.getSendStatus() = " + finalMessage.getSendStatus());
//                message.setSendStatus(finalMessage.getSendStatus());
            }

        } catch (Exception e) {
            if( message!=null ) {
                message.setSendStatus("2");
                updateMessageStatus(message);
            }
            System.out.println("e = " + e.getMessage());
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error!" );
        }
//        if( message!=null && "1".equalsIgnoreCase(message.getSendStatus()))
        if(mobileNos.length==1)
            return new ResponseMessage(HttpStatus.OK,"Message Sent to "+msg.getToMobileNumber() );
        else
            return new ResponseMessage(HttpStatus.OK,"Messages Sent Successfully." );
//        else
//            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Message Send failed to "+msg.getToMobileNumber() );
    }


    /*public synchronized ResponseMessage test(MessageDTO msg)  {
        System.out.println("service test() start"+msg.getToMobileNumber() );
        try {
            Thread.sleep(Duration.ofSeconds(3));
        }catch (Exception e){
            System.out.println("e = " + e);
        }
        System.out.println("service test() end"+msg.getToMobileNumber() );
        return new ResponseMessage(HttpStatus.OK,"Message Sent."+msg.getToMobileNumber() );
    }*/


}
