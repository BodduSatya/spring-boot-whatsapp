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
//@Table(uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"tomobilenumber", "message", "mediaurl", "createdon_date"})
//})
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

    // Additional column to store only the date part of 'createdon'
//    @Transient
    @Column(name = "createdon_date", nullable = false)
    private String createdonDate;

    @PrePersist
    public void prePersist() {
        this.createdonDate = createdon.toLocalDate().toString(); // Extract only the date part
    }

    public Message(String toMobileNumber,String message,String typeOfMsg,LocalDateTime createdon,
                   String mediaUrl,String caption,long id, String fileName){
        this.toMobileNumber = toMobileNumber;
        this.message = message;
        this.typeOfMsg = typeOfMsg;
        this.createdon = createdon;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.id = id;
        this.createdonDate = createdon.toLocalDate().toString(); // Initialize createdonDate
        this.fileName = fileName; // Initialize createdonDate
    }

}
