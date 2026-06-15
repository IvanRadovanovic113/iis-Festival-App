package com.festivalapp.repository;

import com.festivalapp.model.NegotiationConditionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationConditionValueRepository extends JpaRepository<NegotiationConditionValue, Long> {
    List<NegotiationConditionValue> findByNegotiation_Id(Long negotiationId);
}