package com.festivalapp.repository;

import com.festivalapp.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    boolean existsByFestival_FestivalId(Long festivalId);

    Optional<Campaign> findByFestival_FestivalId(Long festivalId);
}
