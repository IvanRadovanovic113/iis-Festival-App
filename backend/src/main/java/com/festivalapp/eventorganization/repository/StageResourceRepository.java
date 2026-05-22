package com.festivalapp.eventorganization.repository;

import com.festivalapp.eventorganization.model.StageResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StageResourceRepository extends JpaRepository<StageResource, Long> {
    List<StageResource> findByStage_StageIdOrderByResource_NameAsc(Long stageId);
    Optional<StageResource> findByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    boolean existsByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    void deleteByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    void deleteByResource_Id(Long resourceId);
}
