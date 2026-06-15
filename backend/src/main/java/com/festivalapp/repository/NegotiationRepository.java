package com.festivalapp.repository;

import com.festivalapp.model.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long>, JpaSpecificationExecutor<Negotiation> {
    List<Negotiation> findByStartedBy_Id(Long managerId);
}