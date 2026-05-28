package com.festivalapp.repository;

import com.festivalapp.model.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    // Izvlači sve tranzicije unutar jednog šablona prateći vezu preko source stanja
    @Query("SELECT t FROM WorkflowTransition t WHERE t.sourceState.template.id = :templateId")
    List<WorkflowTransition> findAllTransitionsByTemplateId(@Param("templateId") Long templateId);
}