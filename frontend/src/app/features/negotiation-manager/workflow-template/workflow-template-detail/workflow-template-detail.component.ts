import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';
import { WorkflowTemplateDetailResponse } from '../../../../core/models/workflow.model';

@Component({
  selector: 'app-workflow-template-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './workflow-template-detail.component.html',
  styleUrls: ['./workflow-template-detail.component.css']
})
export class WorkflowTemplateDetailComponent implements OnInit {
  
  templateId!: number;
  template: WorkflowTemplateDetailResponse | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private workflowService: WorkflowTemplateService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.templateId = +idParam;
      this.loadTemplateDetails();
    } else {
      this.errorMessage = 'Invalid template ID.';
    }
  }

  loadTemplateDetails(): void {
    this.isLoading = true;
    this.workflowService.getTemplateById(this.templateId).subscribe({
      next: (data) => {
        data.states.sort((a, b) => {
          if (a.initial) return -1;
          if (b.initial) return 1;
          if (a.finalState) return 1;
          if (b.finalState) return -1;
          return 0;
        });
        this.template = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading template details', err);
        this.errorMessage = 'Could not load workflow template details.';
        this.isLoading = false;
      }
    });
  }

  // Pomoćna funkcija za mapiranje imena stanja preko ID-ja (rešava problem u tabeli)
  getStateName(stateId: number): string {
    if (!this.template) return `State ${stateId}`;
    const state = this.template.states.find(s => s.id === stateId);
    return state ? state.name : `State ${stateId}`;
  }

  onArchive(): void {
    if (this.template && confirm('Are you sure you want to archive this template?')) {
      this.workflowService.archiveTemplate(this.template.id).subscribe({
        next: () => {
          alert('Template archived successfully.');
          this.loadTemplateDetails();
        },
        error: (err) => alert(err.error?.message || 'Failed to archive template.')
      });
    }
  }
}