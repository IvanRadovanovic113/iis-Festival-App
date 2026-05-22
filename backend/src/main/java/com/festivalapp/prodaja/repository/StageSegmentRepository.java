package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.StageSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StageSegmentRepository extends JpaRepository<StageSegment, Long> {
    List<StageSegment> findByStage_StageId(Long stageId);
    Optional<StageSegment> findByStage_StageIdAndSegment_SegmentId(Long stageId, Long segmentId);
    boolean existsByStage_StageIdAndSegment_SegmentId(Long stageId, Long segmentId);
    void deleteByStage_StageIdAndSegment_SegmentId(Long stageId, Long segmentId);
    void deleteBySegment_SegmentId(Long segmentId);
}
