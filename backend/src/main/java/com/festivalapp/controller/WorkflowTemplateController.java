package com.festivalapp.controller;

import com.festivalapp.dto.WorkflowTemplateDetailResponse;
import com.festivalapp.dto.WorkflowTemplateRequest;
import com.festivalapp.dto.WorkflowTemplateResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.WorkflowTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/negotiation-manager/workflow-templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    // 1. Kreiranje novog šablona radnog toka (struktura faza, tranzicija i uslova)
    @PostMapping
    public ResponseEntity<WorkflowTemplateDetailResponse> createTemplate(
            @Valid @RequestBody WorkflowTemplateRequest request,
            @AuthenticationPrincipal User user
    ) {
        WorkflowTemplateDetailResponse response = workflowTemplateService.createTemplate(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Pregled aktivnih ili arhiviranih šablona uz pretragu i paginaciju
    // Primer: /api/negotiation-manager/workflow-templates?archived=false&searchTerm=Glavna&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<WorkflowTemplateResponse>> getTemplates(
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<WorkflowTemplateResponse> templates = workflowTemplateService.getTemplates(archived, searchTerm, pageable, user);
        return ResponseEntity.ok(templates);
    }

    // 3. Detaljan grafički prikaz konkretnog šablona (sva stanja, tranzicije i uslovi)
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowTemplateDetailResponse> getTemplateById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        WorkflowTemplateDetailResponse response = workflowTemplateService.getTemplateById(id, user);
        return ResponseEntity.ok(response);
    }

    // 4. Kreiranje nove verzije postojećeg šablona (Ad-hoc kopiranje celog grafa)
    @PostMapping("/{id}/copy")
    public ResponseEntity<WorkflowTemplateDetailResponse> createNewVersion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        WorkflowTemplateDetailResponse response = workflowTemplateService.createNewVersion(id, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 5. Arhiviranje šablona radnog toka (samo ako nema aktivnih pregovora)
    @PatchMapping("/{id}/archive")
    public ResponseEntity<WorkflowTemplateResponse> archiveTemplate(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        WorkflowTemplateResponse response = workflowTemplateService.archiveTemplate(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WorkflowTemplateResponse>> getAllTemplates(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(workflowTemplateService.findAllActive());
    }
}