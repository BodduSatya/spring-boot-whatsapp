package org.satya.whatsapp.listener;

import it.auties.whatsapp.api.DisconnectReason;
import it.auties.whatsapp.api.SocketEvent;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.contact.Contact;
import it.auties.whatsapp.model.contact.ContactJid;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.standard.TextMessage;
import it.auties.whatsapp.model.request.Node;
import lombok.extern.slf4j.Slf4j;
import org.satya.whatsapp.modal.MailRequest;
import org.satya.whatsapp.service.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class MyListener implements Listener {

    private final String WELCOME_MSG="Thank you for your interest, now we will communicate with you from this chat window.";

    private final EmailService emailService;

    public MyListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onLoggedIn(Whatsapp w) {
//        System.out.println("-> Logged in!");
        //System.out.println(w.store().toJson());

        // Print a message when connected
        System.out.printf("Connected: %s%n", w.store().privacySettings());

    }

    public void onDisconnected(DisconnectReason reason) {
//        System.out.println("-> Logged in!");
        //System.out.println(w.store().toJson());

        // Print a message when disconnected
        System.out.printf("Disconnected: %s%n", reason);

        try {
            // push email
            Map<String, Object> modal = new HashMap<>();
            modal.put("name", "wa server disconnected on "+ DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a").format(LocalDateTime.now()) +" with reason :: ");
            modal.put("location", reason);

            MailRequest mailRequest = new MailRequest();
            mailRequest.setTo("satyanarayana@sanharabs.com");
            mailRequest.setSubject("Wa disconnected ");
            mailRequest.setName("Satya");
            emailService.sendEmail(mailRequest, modal);

        }catch (Exception e){
            log.error(" Exception in onDisconnected() " , e);
            System.out.println("Exception in onDisconnected() ==> " + e.getMessage());
        }
    }

    @Override
    public void onContacts(Whatsapp whatsapp, Collection<Contact> contacts) {
        System.out.printf("Contacts: %s%n", contacts.size());
    }

    @Override
    public void onNewMessage(Whatsapp w, MessageInfo info) {
        //log.info("*** Message received... *** {} \n ", info.toJson());

        if (!(info.message().content() instanceof TextMessage textMessage)) {
            return;
        }

        /*
        System.out.println("1 :" + info.senderJid().user()); //1 :91xxxxxxxxxx ( sender ph no )
        System.out.println("2 :" + info.senderName());      //2  : contact name in whatsapp
        System.out.println("3 :" + info.chatJid().user()); //3 :  91xxxxxxxxxx ( recipient ph no )
        System.out.println("4 :" + info.chatJid().toPhoneNumber()); //4 :+91xxxxxxxxxx
        info.sender().ifPresent(contact -> System.out.println("5: " + contact.chosenName() + " " + contact.name() + " " + contact.jid())); //5: name 91xxxxxxxxxx@s.whatsapp.net
        System.out.println("6: " + textMessage.text());    // 6: message text
        System.out.println("7: " + info.chat().jid());   // 7: 91xxxxxxxxxx@s.whatsapp.net
        */

        System.out.println( "*** Message received from "+info.chatJid().user() );

        if(Arrays.asList(new String[]{"Hi","Hello","Hai","Hey"}).contains(textMessage.text())){
            var chat = w.store()
                    .findChatByJid(ContactJid.of(info.chatJid().toPhoneNumber()))
                    .orElseThrow(() -> new NoSuchElementException("Hey," + info.chatJid().toPhoneNumber() + " not exist"));

            w.sendMessage(chat, WELCOME_MSG);
        }
//        else if( textMessage.text().contains("#")){
//            System.out.println("else = ");
//            try {
//                String res = chatGPTService.getChatResponse(textMessage.text());
//                System.out.println("4res = " + res);
//                if( res!=null && !res.isEmpty() ) {
//                    var chat = w.store()
//                            .findChatByJid(ContactJid.of(info.chatJid().toPhoneNumber()))
//                            .orElseThrow(() -> new NoSuchElementException("Hey," + info.chatJid().toPhoneNumber() + " not exist"));
//
//                    w.sendMessage(chat, res);
//                }
//
//            }catch (Exception e){
//                System.out.println("e = " + e);
//            }
//        }

//        System.out.println("---");
//        System.out.println(info.toJson());
//        System.out.println("---");
//        System.out.println("*** Finished messages receiving ***");

    }

    @Override
    public void onStatus(Whatsapp whatsapp, Collection<MessageInfo> status) {

    }


    @Override
    public void onSocketEvent(Whatsapp whatsapp, SocketEvent event) {

    }

    @Override
    public void onNodeReceived(Whatsapp whatsapp, Node incoming) {

    }

    @Override
    public void onNodeSent(Whatsapp whatsapp, Node incoming) {
    }


}
