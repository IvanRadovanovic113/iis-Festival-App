package com.festivalapp.repository;

import com.festivalapp.model.TransitionCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransitionConditionRepository extends JpaRepository<TransitionCondition, Long> {

    // Provera jedinstvenosti ključa pre čuvanja u katalog
    boolean existsByConditionKey(String conditionKey);

    // Pretraga uslova iz kataloga po tekstu labele (Case-Insensitive)
    Page<TransitionCondition> findByLabelContainingIgnoreCase(String label, Pageable pageable);
}