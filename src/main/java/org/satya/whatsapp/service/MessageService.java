package org.satya.whatsapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.jid.Jid;
import it.auties.whatsapp.model.message.standard.*;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.satya.whatsapp.entity.Message;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.repository.MessageCriteriaRepository;
import org.satya.whatsapp.repository.MessageRepository;
import org.satya.whatsapp.utils.MediaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private static final Logger log = LogManager.getLogger(MessageService.class);
    private final Whatsapp whatsappApi;
    private final ModelMapper modelMapper;
    private final MessageRepository messageRepository;
    private final MessageCriteriaRepository messageCriteriaRepository;

    public MessageService(Whatsapp whatsappApi, ModelMapper modelMapper, MessageRepository messageRepository,
                          MessageCriteriaRepository messageCriteriaRepository) {
        this.whatsappApi = whatsappApi;
        this.modelMapper = modelMapper;
        this.messageRepository = messageRepository;
        this.messageCriteriaRepository = messageCriteriaRepository;
    }

    @Transactional
    public synchronized ResponseMessage sendMessageV3(MessageDTO msg){
        Message message = null;
        String[] mobileNos = null;
        try {
            mobileNos = msg.getToMobileNumber().split(",");

            for (String mobileNo : mobileNos) {
                msg.setToMobileNumber(mobileNo);
                if ( !"text".equalsIgnoreCase(msg.getTypeOfMsg())
                        && msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0 ) {
                    for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                        message = new Message(msg.getToMobileNumber(), msg.getMessage(), msg.getTypeOfMsg(),
                                LocalDateTime.now(), msg.getMediaUrl2()[k], k ==0 ? msg.getCaption() : "", msg.getId(),
                                msg.getFileName2()[k]
                        );

                        if( msg.getId() == 0 )
                            saveMessage(message);
                    }
                }
                else{
                    message = new Message(msg.getToMobileNumber(), msg.getMessage(), msg.getTypeOfMsg(),
                            LocalDateTime.now(), msg.getMediaUrl(), msg.getCaption(), msg.getId(),"");

                    if( msg.getId() == 0 )
                        saveMessage(message);
                }

                Jid contactJid = Jid.of(msg.getToMobileNumber());
                System.out.print("\ncontactJid = " + contactJid);

                Chat chat = null;
                try {
                    if(msg.isGroupMsg()) {
                        chat = whatsappApi.store()
                                .findChatByJid(contactJid)
                                .filter(Chat::isGroup)
                                .orElseThrow(() ->new RuntimeException(msg.getToMobileNumber() + " chat not found or is not a group chat!"));
                    }else{
                        chat = whatsappApi.store()
                                .findChatByJid(contactJid)
                                .orElseThrow(() -> new RuntimeException(msg.getToMobileNumber() + " chat not found!") );
                    }
                } catch (RuntimeException e) {
                    System.out.println("e = " + e);
                }

//                System.out.println("chat = " + chat);
                //text / image / audio  / video / gif / document / reaction / remove_reaction
                switch (msg.getTypeOfMsg()) {
                    case "text" -> {
                        if (msg.getMessage() != null && !msg.getMessage().isEmpty()) {
                            var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, msg.getMessage()).join();
                            updateMsgStatus(messageInfo,message);
                        } else
                            log.info(" Missing required parameter message");
                    }
                    case "image" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending image...");

                                var image = new ImageMessageSimpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
//                                        .thumbnail(mediabytes)
                                        .caption(k == 0 ? msg.getCaption() : "")
//                                        .mimeType("image/png")
                                        .build();

                                var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, image).join();
                                updateMsgStatus(messageInfo,message);

                                if (msg.isLogRequired()) log.info("Sent image");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "document" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending document...");
                                var document = new DocumentMessageSimpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .title(k == 0 ? msg.getCaption() : "")
                                        .fileName( msg.getFileName2()[k] )
//                              .pageCount(1)
                                        .build();
                                var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, document).join();
                                updateMsgStatus(messageInfo,message);
                                if (msg.isLogRequired()) log.info("Sent document");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "audio" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending audio...");
                                var audio = new AudioMessageSimpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .voiceMessage(true)
                                        .build();
                                var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, audio).join();
                                updateMsgStatus(messageInfo,message);
                                if (msg.isLogRequired()) log.info("Sent audio");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "video" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending video...");
                                var video = new VideoMessageSimpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .caption(msg.getCaption()).build();
                                var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, video).join();
                                updateMsgStatus(messageInfo,message);
                                if (msg.isLogRequired()) log.info("Sent video");
                            }
                        } else
                            log.info(" Missing required parameter mediaUrl ");
                    }
                    case "gif" -> {
                        if (msg.getMediaUrl2() != null && msg.getMediaUrl2().length > 0) {
                            for (int k = 0; k < msg.getMediaUrl2().length; k++) {
                                if (msg.isLogRequired()) log.info("Sending gif...");
                                var video = new GifMessageSimpleBuilder()
                                        .media(MediaUtils.readBytes(msg.getMediaUrl2()[k]))
                                        .caption(msg.getCaption()).build();
                                var messageInfo = whatsappApi.sendMessage(chat!=null ? chat : contactJid, video).join();
                                updateMsgStatus(messageInfo,message);
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
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Exception in MessageService.sendMessageV3() ==>> InterruptedException = " + e.getMessage());
                }
            }
        } catch (Exception e) {
            if( message!=null ) {
                message.setSendStatus("2");
                updateMessageStatus(message);
            }
            System.out.println("Exception in MessageService.sendMessageV3() ==>> " + e.getMessage());
//            e.printStackTrace();
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage() );
        }
        if(mobileNos.length==1)
            return new ResponseMessage(HttpStatus.OK,"Message Sent to "+msg.getToMobileNumber() );
        else
            return new ResponseMessage(HttpStatus.OK,"Messages Sent Successfully." );
    }

    public void saveMessage(Message message){
        long count ;

        if( "text".equalsIgnoreCase(message.getTypeOfMsg()) ) {
            count = messageRepository.countByMessageText(
                    message.getMessage() != null ? message.getMessage().trim().toUpperCase() : "",
                    message.getToMobileNumber(),
                    message.getCreatedonDate()
            );
        }
        else{
            count = messageRepository.countByMediaUrl(
                    message.getMediaUrl() != null ? message.getMediaUrl().trim().toUpperCase() : "",
                    message.getToMobileNumber(),
                    message.getCreatedonDate()
            );
        }

//        System.out.println("count = " + count);
        if (count > 0) {
            throw new RuntimeException("Duplicate message with in the same day & same contact not allowed.");
        }

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
            log.error("Failed to update message status in updateMessageStatus() = ", e);
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
                .distinct()
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

        log.info("MIDS - {}  ", (Object) mids);

        List<Message> messages = null;
        if( mids!=null && mids.length >0 ){
            messages = messageRepository.getNonSendMessagesByMIDs(mids);
        }

        assert messages != null;
        return messages.stream()
                .map((message)->modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    private void updateMsgStatus(MessageInfo result, Message finalMessage ){
        String status = "";
        if( result!=null)
            status = extractValueFromJson(result.toJson());
        System.out.println("  ==> " + status );
        finalMessage.setSendStatus( "PENDING".contains(status.trim()) || "SERVER_ACK".contains(status.trim()) || "PENDING".equalsIgnoreCase(status.trim()) || "SERVER_ACK".equalsIgnoreCase(status.trim()) ? "1" : "2");
        updateMessageStatus(finalMessage);
    }

    private String extractValueFromJson(String result){
        try {
//            log.info("Result => {}",result);
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            JsonNode valueNode = jsonNode.get("status");
            if (valueNode != null) {
                return valueNode.asText(); // Use appropriate method based on value type
            } else {
                return "";
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public int deleteMessages(String[] mids){
        try {
            List<Long> longArray = convertIterableStringToLong(mids);
//            longArray.forEach(System.out::println);
            return messageRepository.deleteAllByIdInBatchAndReturnCount(longArray);
        } catch (Exception e) {
            System.out.println("deleteMessages e = " + e);
            log.error("Failed to update message status in deleteMessages() = ", e);
        }
        return 0;
    }

    public static List<Long> convertIterableStringToLong(String[] iterableString) {
        List<Long> longList = new ArrayList<>();
        for (String str : iterableString) {
            try {
                longList.add(Long.parseLong(str));
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format: " + str);
            }
        }
        return longList;
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

}
