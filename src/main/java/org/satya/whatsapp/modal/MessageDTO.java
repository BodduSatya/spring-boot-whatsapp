package org.satya.whatsapp.modal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private long id;
    private String toMobileNumber;
    private String message;
    private String typeOfMsg; // text / image / audio  / video / gif / document / reaction / remove_reaction

    private String mediaUrl;
    private String caption;
    private String fileName;

    private String sendStatus;
    private LocalDateTime createdon;
    private LocalDateTime senton;

    private boolean logRequired=false;
}
