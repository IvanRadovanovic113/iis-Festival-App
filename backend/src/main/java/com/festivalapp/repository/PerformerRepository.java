package com.festivalapp.repository;

import com.festivalapp.model.Performer;
import com.festivalapp.model.PerformerStatus;
import com.festivalapp.model.PerformerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerformerRepository extends JpaRepository<Performer, Long> {

    boolean existsByStageNameIgnoreCase(String stageName);

    boolean existsByStageNameIgnoreCaseAndPerformerIdNot(String stageName, Long performerId);

    @Query("SELECT p FROM Performer p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:genre IS NULL OR LOWER(p.genre) = LOWER(CAST(:genre AS string))) AND " +
           "(:performerType IS NULL OR p.performerType = :performerType) AND " +
           "(:countryOfOrigin IS NULL OR LOWER(p.countryOfOrigin) = LOWER(CAST(:countryOfOrigin AS string))) AND " +
           "(:numberOfMembers IS NULL OR p.numberOfMembers = :numberOfMembers) AND " +
           "(:searchName IS NULL OR LOWER(p.stageName) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%')))")
    Page<Performer> findByFilters(
            @Param("status") PerformerStatus status,
            @Param("genre") String genre,
            @Param("performerType") PerformerType performerType,
            @Param("countryOfOrigin") String countryOfOrigin,
            @Param("numberOfMembers") Integer numberOfMembers,
            @Param("searchName") String searchName,
            Pageable pageable
    );
}