package org.satya.whatsapp;

import org.satya.whatsapp.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class WhatsappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsappApplication.class, args);
    }

}
