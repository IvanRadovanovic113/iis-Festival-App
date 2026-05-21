package com.festivalapp.eventorganization.repository;

import com.festivalapp.eventorganization.model.EventResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventResourceRepository extends JpaRepository<EventResource, Long> {
    List<EventResource> findByFestival_FestivalIdOrderByNameAsc(Long festivalId);
    boolean existsByNameIgnoreCaseAndFestival_FestivalId(String name, Long festivalId);
    boolean existsByNameIgnoreCaseAndFestival_FestivalIdAndIdNot(String name, Long festivalId, Long id);
}
