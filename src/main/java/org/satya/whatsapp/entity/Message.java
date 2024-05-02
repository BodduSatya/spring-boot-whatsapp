package org.satya.whatsapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="MESSAGES")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="tomobilenumber")
    private String toMobileNumber;

    private String message;

    @Column(name="typeofmsg")
    private String typeOfMsg; // text / image / audio  / video / gif / document / reaction / remove_reaction

    @Column(name="mediaurl")
    private String mediaUrl;
    private String caption;

    @Column(name="filename")
    private String fileName;

    @Column(name="sendstatus")
    private String sendStatus;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdon;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime senton;

    public Message(String toMobileNumber,String message,String typeOfMsg,LocalDateTime createdon, String mediaUrl,String caption,long id){
        this.toMobileNumber = toMobileNumber;
        this.message = message;
        this.typeOfMsg = typeOfMsg;
        this.createdon = createdon;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.id = id;
    }

}
