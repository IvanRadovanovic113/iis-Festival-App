package com.festivalapp.repository;

import com.festivalapp.model.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    List<Ad> findAllByCampaign_CampaignIdOrderByLastChangeDateDescAdIdDesc(Long campaignId);
}
