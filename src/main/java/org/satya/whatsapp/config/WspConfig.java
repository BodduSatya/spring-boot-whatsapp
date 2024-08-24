package org.satya.whatsapp.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.WebHistoryLength;
import it.auties.whatsapp.api.Whatsapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WspConfig {

    @Value("${wa.config.dir}")
    String filePath;

//    @Value("${wa.config.phoneNumber}")
//    long phoneNumber;

//    @Autowired
//    EmailService emailService;

//    @Autowired
//    ChatGPTService chatGPTService;

    private static final Logger log = LogManager.getLogger(WspConfig.class);

    // Web API QR Pairing
    @Bean
    public Whatsapp whatsappApi() throws IOException {
        System.out.println("* WspConfig.whatsappApi() configuration initiating from file "+filePath);
        Whatsapp wsp = null;

        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path);

//            wsp = Whatsapp.webBuilder() // Use the Web api
//                    .serializer(new DefaultControllerSerializer(path))
//                    .lastConnection() // Deserialize the last connection, or create a new one if it doesn't exist
//                    .errorHandler(ErrorHandler.toTerminal())
//                    .unregistered(QrHandler.toTerminal()) // Print the pairing code to the terminal
//                    .addListener(new MyListener(emailService)) // Print a message when a new chat message arrives
//                    .connect() // Connect to Whatsapp asynchronously
//                    .join(); // Await the result

            wsp = Whatsapp.webBuilder()
//                    .serializer(new ProtobufControllerSerializer(path))
//                    .serializer(new DiscardingControllerSerializer(path))
//                    .serializer(new ProtobufControllerSerializer(path))
//                    .serializer(new ControllerSerializer(path))
                    .lastConnection()
                    .historyLength(WebHistoryLength.extended())
                    .unregistered(QrHandler.toTerminal())
                    .addLoggedInListener(api -> System.out.printf("Connected: %s%n", api.store().privacySettings()))
                    .addFeaturesListener(features -> { /*log.info("Received features: {} ",features);*/ })
//                    .addNewChatMessageListener((api, message) -> System.out.println())
                    .addContactsListener((api, contacts) -> System.out.printf("Contacts: %s%n", contacts.size()))
                    .addChatsListener((api, chats) -> { /*log.info("Chats: {}", chats);*/ }  )
//                    .addNewslettersListener((api, newsletters) -> System.out.printf("Newsletters: %s%n", newsletters.size()))
                    .addNodeReceivedListener(incoming -> { /*log.info("Received node {}", incoming);*/ }  )
                    .addNodeSentListener(outgoing -> { /*log.info("Sent node {}", outgoing);*/ } )
                    .addActionListener ((action, info) -> { /*log.info("New action: {}, info: {}", action, info);*/ })
                    .addSettingListener(setting -> { /*log.info("New setting:{}", setting);*/ } )
                    .addContactPresenceListener((chat, contact, status) -> { /*log.info("Status of {} changed in {} to {}", contact, chat.name(), status.name());*/ } )
//                    .addMessageStatusListener((info) -> System.out.printf("Message status update for %s%n", info.id()))
                    .addChatMessagesSyncListener((api, chat, last) -> {
                        /*log.info("{} now has {} messages: {} (oldest message: {})", chat.name(), chat.messages().size(), !last ? "waiting for more" : "done",""
                            //chat.oldestMessage().flatMap(ChatMessageInfo::timestamp).orElse(null)
                        ); */
                    })
                    .addDisconnectedListener(reason -> System.out.printf("Disconnected: %s%n", reason))
                    .connect()
                    .join();

            System.out.println("* WspConfig.whatsappApi() configuration done successfully!");
        }catch (Exception e){
            System.out.println("Exception in WspConfig.whatsappApi() ==>> " + e.getMessage());
        }
        return wsp;
    }

   /* // Web API PairCode
    @Bean
    public Whatsapp whatsappApi() throws IOException {

//        System.out.println("Enter the phone number(include the country code prefix, but no +, spaces or parenthesis):");
//        var scanner = new Scanner(System.in);
//        var phoneNumber = scanner.nextLong();

        Whatsapp wsp = Whatsapp.webBuilder() // Use the Web api
                .lastConnection() // Deserialize the last connection, or create a new one if it doesn't exist
                .errorHandler(ErrorHandler.toTerminal())
                .unregistered(phoneNumber, PairingCodeHandler.toTerminal()) // Print the pairing code to the terminal
                .addListener(new MyListener()) // Print a message when a new chat message arrives
                .connect() // Connect to Whatsapp asynchronously
                .join(); // Await the result

        System.out.println("* WspConfig.whatsappApi()");

        return wsp;
    }*/
}
