package org.satya.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WhatsappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsappApplication.class, args);
    }

    @GetMapping("/")
    @ResponseBody
    public String welcome() {
        return "<html>" +
                    "<head><title>Whatsapp API Home</title></head>"+
                    "<body style='margin: auto;'>"+
                        "<div style='color:white;background-color:#00a884;height:25%'>" +
                            "<h2 style='margin-left: 10%;padding-top: 5%;'>" +
                                "Welcome come to WhatsApp API, Version - 1.0" +
                                "<h5 style='margin-left: 10%;'>"+
                                    "Last updated on : 26/01/2024." +
                                "</h5>"+
                            "</h2>" +
                        "</div>"+
                    "</body>"+
                "</html>";
    }

}
