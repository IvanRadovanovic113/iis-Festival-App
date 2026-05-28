package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.eventorganization.StageResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StageResourceRepository extends JpaRepository<StageResource, Long> {
    List<StageResource> findByStage_StageIdOrderByResource_NameAsc(Long stageId);
    Optional<StageResource> findByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    boolean existsByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    boolean existsByStage_StageIdAndResource_NameIgnoreCase(Long stageId, String resourceName);
    boolean existsByResource_Id(Long resourceId);
    @Query("""
        select count(stageResource) > 0
        from StageResource stageResource
        where lower(stageResource.resource.name) = lower(:resourceName)
          and stageResource.resource.id <> :resourceId
          and stageResource.stage.stageId in (
              select assignedStage.stage.stageId
              from StageResource assignedStage
              where assignedStage.resource.id = :resourceId
          )
    """)
    boolean existsDuplicateResourceNameOnAssignedStages(
        @Param("resourceId") Long resourceId,
        @Param("resourceName") String resourceName
    );
    void deleteByStage_StageIdAndResource_Id(Long stageId, Long resourceId);
    void deleteByResource_Id(Long resourceId);
}
