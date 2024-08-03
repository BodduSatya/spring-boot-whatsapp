package org.satya.whatsapp.modal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    private String createdon;
    private String senton;

    private boolean logRequired=false;

    public MessageDTO(String toMobileNumber, String typeOfMsg, String message, String mediaUrl,
                      String caption,String fileName) {
        this.toMobileNumber = toMobileNumber;
        this.typeOfMsg = typeOfMsg;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.fileName = fileName;
    }

    public MessageDTO(String toMobileNumber, String typeOfMsg, String message, String mediaUrl,
                      String caption,String fileName,LocalDateTime createdon, LocalDateTime senton,long id,
                      String sendStatus) {
        this.toMobileNumber = toMobileNumber;
        this.typeOfMsg = typeOfMsg;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.fileName = fileName;
        this.createdon = createdon!=null ? createdon.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")):""; // Adjust the pattern as needed
        this.senton = senton!=null ? senton.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")):""; // Adjust the pattern as needed
        this.id = id;
        this.sendStatus = sendStatus;
    }

    // Override equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDTO that = (MessageDTO) o;
        return Objects.equals(toMobileNumber, that.toMobileNumber) &&
                Objects.equals(message, that.message);
    }

    // Override hashCode
    @Override
    public int hashCode() {
        return Objects.hash(toMobileNumber, message);
    }
}
