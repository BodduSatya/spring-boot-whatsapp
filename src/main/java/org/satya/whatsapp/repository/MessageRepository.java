package org.satya.whatsapp.repository;

import org.satya.whatsapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate")
    List<Message> getAllMessagesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate AND e.sendStatus ='0' ")
    List<Message> getAllNonSendMessagesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM MESSAGES e WHERE e.createdon BETWEEN :startDate AND :endDate AND e.sendStatus ='0' AND e.toMobileNumber = :mobileNo ")
    List<Message> getAllNonSendMessagesBetweenDatesMobNo(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate , @Param("mobileNo") String mobileNo);

}
