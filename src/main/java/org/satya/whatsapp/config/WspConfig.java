package org.satya.whatsapp.config;

import it.auties.whatsapp.api.*;
import it.auties.whatsapp.controller.DefaultControllerSerializer;
import org.satya.whatsapp.listener.MyListener;
import org.satya.whatsapp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Autowired
    EmailService emailService;

//    @Autowired
//    ChatGPTService chatGPTService;

    // Web API QR Pairing
    @Bean
    public Whatsapp whatsappApi() throws IOException {
        System.out.println("* WspConfig.whatsappApi() => "+filePath);

        Path path = Paths.get(filePath);
        Files.createDirectories(path);

        Whatsapp wsp = Whatsapp.webBuilder() // Use the Web api
                .serializer(new DefaultControllerSerializer(path))
                .lastConnection() // Deserialize the last connection, or create a new one if it doesn't exist
                .errorHandler(ErrorHandler.toTerminal())
                .unregistered(QrHandler.toTerminal()) // Print the pairing code to the terminal
                .addListener(new MyListener(emailService)) // Print a message when a new chat message arrives
                .connect() // Connect to Whatsapp asynchronously
                .join(); // Await the result

        System.out.println("* WspConfig.whatsappApi()");

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
