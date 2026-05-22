package com.festivalapp.repository;

import com.festivalapp.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findByFestival_FestivalId(Long festivalId);
    boolean existsByNameAndFestival_FestivalId(String name, Long festivalId);
    boolean existsByNameAndFestival_FestivalIdAndStageIdNot(String name, Long festivalId, Long stageId);
}
