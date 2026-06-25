package com.festivalapp.repository;

import com.festivalapp.model.WorkflowState;
import com.festivalapp.model.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
    
    // Izvlači sva stanja koja pripadaju jednom šablonu radnog toka
    List<WorkflowState> findByTemplateId(Long templateId);

    Optional<WorkflowState> findByTemplateAndInitial(WorkflowTemplate template, boolean initial);
    Optional<WorkflowState> findByTemplateAndFinalState(WorkflowTemplate template, boolean finalState);
}