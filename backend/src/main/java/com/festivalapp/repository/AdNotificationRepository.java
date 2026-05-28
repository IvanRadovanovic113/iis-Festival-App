package com.festivalapp.repository;

import com.festivalapp.model.AdNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdNotificationRepository extends JpaRepository<AdNotification, Long> {

    List<AdNotification> findAllByRecipientUser_IdOrderByCreatedAtDescNotificationIdDesc(Long recipientUserId);

    void deleteAllByAd_AdId(Long adId);
}
