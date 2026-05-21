package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.BinaSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BinaSegmentRepository extends JpaRepository<BinaSegment, Long> {
    List<BinaSegment> findByBina_BinaId(Long binaId);
    Optional<BinaSegment> findByBina_BinaIdAndSegment_SegmentId(Long binaId, Long segmentId);
    boolean existsByBina_BinaIdAndSegment_SegmentId(Long binaId, Long segmentId);
    void deleteByBina_BinaIdAndSegment_SegmentId(Long binaId, Long segmentId);
    void deleteBySegment_SegmentId(Long segmentId);
}
