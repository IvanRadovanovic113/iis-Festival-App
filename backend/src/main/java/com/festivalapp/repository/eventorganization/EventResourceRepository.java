package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.eventorganization.EventResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventResourceRepository extends JpaRepository<EventResource, Long> {
    List<EventResource> findByFestival_FestivalIdOrderByNameAsc(Long festivalId);
    boolean existsByNameIgnoreCaseAndFestival_FestivalId(String name, Long festivalId);
    boolean existsByNameIgnoreCaseAndFestival_FestivalIdAndIdNot(String name, Long festivalId, Long id);
}
