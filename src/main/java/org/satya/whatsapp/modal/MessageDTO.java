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
    private boolean groupMsg = false;

    private String mediaUrl;
    private String caption;
    private String fileName;

    private String[] mediaUrl2;
    private String[] fileName2;

    private String sendStatus;
    private LocalDateTime createdon;
    private LocalDateTime senton;

    private boolean logRequired=false;

    public MessageDTO(String toMobileNumber, String typeOfMsg, String message, String mediaUrl, String caption,String fileName) {
        this.toMobileNumber = toMobileNumber;
        this.typeOfMsg = typeOfMsg;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.fileName = fileName;
    }
}
