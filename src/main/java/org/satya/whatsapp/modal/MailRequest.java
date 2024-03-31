package org.satya.whatsapp.modal;

import lombok.Data;

@Data
public class MailRequest {
    private String name;
    private String subject;
    private String from;
    private String to;
    private String cc;
    private String bcc;

}