package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.eventorganization.EventResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventResourceRepository extends JpaRepository<EventResource, Long> {
    List<EventResource> findByFestival_FestivalIdOrderByNameAsc(Long festivalId);
    Optional<EventResource> findByFestival_FestivalIdAndNameIgnoreCaseAndTypeIgnoreCaseAndShareable(
        Long festivalId,
        String name,
        String type,
        Boolean shareable
    );
    boolean existsByFestival_FestivalIdAndNameIgnoreCaseAndTypeIgnoreCaseAndShareableAndIdNot(
        Long festivalId,
        String name,
        String type,
        Boolean shareable,
        Long id
    );
}
