package org.satya.whatsapp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.satya.whatsapp.entity.Message;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageCriteriaRepository {

    @Autowired
    private EntityManager entityManager;

    public List<Message> getMessagesBetweenDates(LocalDateTime fromDate, LocalDateTime toDate, String mobileNo, String msgStatus) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> cq = cb.createQuery(Message.class);

        Root<Message> message = cq.from(Message.class);
        List<Predicate> predicates = new ArrayList<>();

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (fromDate != null ) {
//            LocalDateTime from = LocalDateTime.parse(fromDate, formatter);
            predicates.add(cb.greaterThanOrEqualTo(message.get("createdon"), fromDate));
        }

        if (toDate != null ) {
//            LocalDateTime to = LocalDateTime.parse(toDate, formatter);
            predicates.add(cb.lessThanOrEqualTo(message.get("createdon"), toDate));
        }

        if (mobileNo != null && !mobileNo.isEmpty()) {
            predicates.add(cb.equal(message.get("toMobileNumber"), mobileNo));
        }

        if (msgStatus != null && !msgStatus.isEmpty()&& !"-1".equalsIgnoreCase(msgStatus)) {
            predicates.add(cb.equal(message.get("sendStatus"), msgStatus));
        }

        cq.distinct(true).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }
}

