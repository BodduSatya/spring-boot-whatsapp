package org.satya.whatsapp.service;

import it.auties.whatsapp.api.Whatsapp;
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

        List<Message> messages = messageRepository.getAllMessagesBetweenDates(from,to);
//        List<Message> messages = null;
//        if( mobileNo!=null && !mobileNo.trim().isEmpty()){
//            messages = messageRepository.getAllNonSendMessagesBetweenDatesMobNo(from,to,mobileNo);
//        }
//        else{
//            messages = messageRepository.getAllMessagesBetweenDates(from,to);
//        }
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

    public ResponseMessage sendMessageV2(MessageDTO msg){
        try {
            org.satya.whatsapp.entity.Message message = new org.satya.whatsapp.entity.Message(msg.getToMobileNumber(),msg.getMessage(),msg.getTypeOfMsg(), LocalDateTime.now());

            if( msg.getId() == 0 )
                saveMessage(message);

            ContactJid contactJid = ContactJid.builder().server(ContactJid.Server.WHATSAPP).user(msg.getToMobileNumber()).build();

            var chat = whatsappApi.store()
                    .findChatByJid(contactJid)
                    .orElseThrow(() -> {
                                message.setSendStatus("2");
                                updateMessageStatus(message);
                                return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                            }
                    );

            //text / image / audio  / video / gif / document / reaction / remove_reaction
            switch (msg.getTypeOfMsg() ) {
                case "text" -> {
                    if( msg.getMessage()!=null && !msg.getMessage().isEmpty())
                        whatsappApi.sendMessage(chat, msg.getMessage());
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
                        whatsappApi.sendMessage(chat, image).join();
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
                        whatsappApi.sendMessage(chat, document).join();
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
                        whatsappApi.sendMessage(chat, audio).join();
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
                        whatsappApi.sendMessage(chat, video).join();
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
                        whatsappApi.sendMessage(chat, video).join();
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
            System.out.println("e = " + e.getMessage());
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error!" );
        }
        return new ResponseMessage(HttpStatus.OK,"Message Sent." );
    }

    public ResponseMessage sendMessageV3(MessageDTO msg){
        try {
            org.satya.whatsapp.entity.Message message = new org.satya.whatsapp.entity.Message(msg.getToMobileNumber(),msg.getMessage(),msg.getTypeOfMsg(), LocalDateTime.now());

            saveMessage(message);

            ContactJid contactJid = ContactJid.builder().server(ContactJid.Server.WHATSAPP).user(msg.getToMobileNumber()).build();

            var chat = whatsappApi.store()
                    /*.findChatByJid(ContactJid.of(msg.getToMobileNumber()+"@s.whatsapp.net"))
                    .findChatByJid(ContactJid.of("xxxx@g.us"))
                    .findChatByJid(ContactJid.of(msg.getToMobileNumber()))
                    .findChatByName("My Awesome Friend")*/
                    .findChatByJid(contactJid)
                    .orElseThrow(() -> {
                                message.setSendStatus("2");
                                updateMessageStatus(message);
                                return new NoSuchElementException("Hey," + msg.getToMobileNumber() + " not exist");
                            }
                    );

            //text / image / audio  / video / gif / document / reaction / remove_reaction
            switch (msg.getTypeOfMsg() ) {
                case "text" -> {
                    if( msg.getMessage()!=null && !msg.getMessage().isEmpty())
                        whatsappApi.sendMessage(chat, msg.getMessage());
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
                        whatsappApi.sendMessage(chat, image).join();
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
                        whatsappApi.sendMessage(chat, document).join();
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
                        whatsappApi.sendMessage(chat, audio).join();
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
                        whatsappApi.sendMessage(chat, video).join();
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
                        whatsappApi.sendMessage(chat, video).join();
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
            System.out.println("e = " + e.getMessage());
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error!" );
        }
        return new ResponseMessage(HttpStatus.OK,"Message Sent." );
    }

}
