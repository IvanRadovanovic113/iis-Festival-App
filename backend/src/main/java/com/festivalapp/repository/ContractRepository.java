package com.festivalapp.repository;

import com.festivalapp.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query("""
        select contract
        from Contract contract
        join fetch contract.negotiation negotiation
        join fetch negotiation.performer performer
        join fetch negotiation.offer offer
        join fetch contract.stage stage
        where stage.festival.festivalId = :festivalId
          and negotiation.status = com.festivalapp.model.NegotiationStatus.COMPLETED
        order by offer.performanceDate asc, lower(performer.stageName) asc
    """)
    List<Contract> findCompletedStageAssignedByFestival(@Param("festivalId") Long festivalId);

    @Query("select c from Contract c " +
           "join fetch c.negotiation n " +
           "join fetch n.performer p " +
           "join fetch n.offer o " +
           "left join fetch c.stage s " +
           "join fetch c.signedBy u")
    List<Contract> findAllWithDetails();

    @Query("select c from Contract c " +
           "join fetch c.negotiation n " +
           "join fetch n.performer p " +
           "join fetch n.offer o " +
           "left join fetch c.stage s " +
           "join fetch c.signedBy u " +
           "where c.id = :id")
    Optional<Contract> findByIdWithDetails(@Param("id") Long id);
}
