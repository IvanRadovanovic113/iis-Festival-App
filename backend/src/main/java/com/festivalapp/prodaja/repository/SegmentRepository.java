package com.festivalapp.prodaja.repository;

import com.festivalapp.model.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SegmentRepository extends JpaRepository<Segment, Long> {
    List<Segment> findByFestival_FestivalId(Long festivalId);
    boolean existsByNameAndFestival_FestivalId(String name, Long festivalId);
}
