package com.festivalapp.repository;

import com.festivalapp.model.WorkflowTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {

    // Pronalazi sve šablone na osnovu arhiviranog statusa (za primarni i istorijski filter)
    Page<WorkflowTemplate> findByArchived(boolean archived, Pageable pageable);

    // Pretraga šablona po nazivu i statusu arhiviranja (Case-Insensitive)
    Page<WorkflowTemplate> findByArchivedAndNameContainingIgnoreCase(boolean archived, String name, Pageable pageable);

    List<WorkflowTemplate> findByArchivedFalse();
}