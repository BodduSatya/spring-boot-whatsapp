package org.satya.whatsapp.repository;

import jakarta.transaction.Transactional;
import org.satya.whatsapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate")
    List<Message> getAllMessagesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate AND e.toMobileNumber = :mobileNo")
    List<Message> getAllMessagesBetweenDatesMobNo(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("mobileNo") String mobileNo);

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate AND e.sendStatus <>'1' ")
    List<Message> getAllNonSendMessagesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate AND e.sendStatus <>'1' AND e.toMobileNumber = :mobileNo ")
    List<Message> getAllNonSendMessagesBetweenDatesMobNo(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate , @Param("mobileNo") String mobileNo);

    @Query("SELECT e FROM MESSAGES e WHERE e.id in :mids ")
    List<Message> getNonSendMessagesByMIDs( @Param("mids") String[] mids);

    @Query("SELECT COUNT(m) FROM MESSAGES m WHERE " +
            "UPPER(m.message) = UPPER(:message) AND " +
            "UPPER(m.toMobileNumber) = UPPER(:toMobileNumber) AND " +
            "m.createdonDate  = :createdonDate ")
    long countByMessageText(
            @Param("message") String message,
            @Param("toMobileNumber") String toMobileNumber,
            @Param("createdonDate") String createdonDate);

    @Query("SELECT COUNT(m) FROM MESSAGES m WHERE " +
            "UPPER(m.mediaUrl) = UPPER(:mediaUrl) AND " +
            "UPPER(m.toMobileNumber) = UPPER(:toMobileNumber) AND " +
            "m.createdonDate  = :createdonDate ")
    long countByMediaUrl(
            @Param("mediaUrl") String mediaUrl,
            @Param("toMobileNumber") String toMobileNumber,
            @Param("createdonDate") String createdonDate);

    @Transactional
    @Modifying
    @Query("DELETE FROM MESSAGES m WHERE m.id IN :ids")
    int deleteAllByIdInBatchAndReturnCount(List<Long> ids);

}
