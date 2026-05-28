package com.festivalapp.repository;

import com.festivalapp.model.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
    
    // Izvlači sva stanja koja pripadaju jednom šablonu radnog toka
    List<WorkflowState> findByTemplateId(Long templateId);
}