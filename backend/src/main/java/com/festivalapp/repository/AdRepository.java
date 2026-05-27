package com.festivalapp.repository;

import com.festivalapp.model.Ad;
import com.festivalapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    List<Ad> findAllByCampaign_CampaignIdOrderByLastChangeDateDescAdIdDesc(Long campaignId);

    List<Ad> findAllByCampaign_Festival_FestivalIdAndCurrentPhase_AssignedRoleOrderByLastChangeDateDescAdIdDesc(Long festivalId, Role role);

    List<Ad> findAllByCampaign_CampaignIdAndCurrentPhase_AssignedRoleOrderByLastChangeDateDescAdIdDesc(Long campaignId, Role role);
}
