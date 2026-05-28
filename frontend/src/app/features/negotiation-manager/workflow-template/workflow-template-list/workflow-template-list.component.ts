import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';
import { WorkflowTemplateResponse, WorkflowTemplateDetailResponse } from '../../../../core/models/workflow.model';

@Component({
  selector: 'app-workflow-template-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './workflow-template-list.component.html',
  styleUrls: ['./workflow-template-list.component.css']
})
export class WorkflowTemplateListComponent implements OnInit {
  templates: WorkflowTemplateResponse[] = [];
  templateGraphs: { [key: number]: WorkflowTemplateDetailResponse } = {};

  showArchived: boolean = false;
  searchTerm: string = '';
  currentPage: number = 0;
  pageSize: number = 5;
  totalElements: number = 0;
  totalPages: number = 0;
  isLoading: boolean = false;

  constructor(
    private workflowService: WorkflowTemplateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.isLoading = true;
    this.workflowService.getTemplates(this.showArchived, this.searchTerm, this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          this.templates = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;

          this.templates.forEach(t => {
            this.loadTemplateDetails(t.id);
          });
        },
        error: (err) => {
          console.error('Error loading templates', err);
          this.isLoading = false;
        }
      });
  }

  loadTemplateDetails(id: number): void {
    this.workflowService.getTemplateById(id).subscribe({
      next: (details) => {
        details.states.sort((a, b) => {
          if (a.initial) return -1;
          if (b.initial) return 1;
          if (a.finalState) return 1;
          if (b.finalState) return -1;
          return 0;
        });
        this.templateGraphs[id] = details;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadTemplates();
  }

  toggleArchivedFilter(archived: boolean): void {
    this.showArchived = archived;
    this.currentPage = 0;
    this.loadTemplates();
  }

  onCreateVersion(id: number, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/negotiation-manager/workflow-templates/new'], {
      queryParams: { cloneFrom: id }
    });
  }

  onArchive(id: number, event: Event): void {
    event.stopPropagation();
    this.workflowService.archiveTemplate(id).subscribe({
      next: () => {
        this.loadTemplates();
      },
      error: (err) => alert(err.error?.message || 'Failed to archive template. Check if there are active negotiations.')
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadTemplates();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadTemplates();
    }
  }
}