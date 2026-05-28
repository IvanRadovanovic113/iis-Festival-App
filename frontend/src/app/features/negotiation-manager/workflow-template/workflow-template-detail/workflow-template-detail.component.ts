import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';
import { WorkflowTemplateDetailResponse } from '../../../../core/models/workflow.model';
import { Network, Node, Edge, Options } from 'vis-network';
import { DataSet } from 'vis-data';

@Component({
  selector: 'app-workflow-template-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './workflow-template-detail.component.html',
  styleUrls: ['./workflow-template-detail.component.css']
})
export class WorkflowTemplateDetailComponent implements OnInit {
  @ViewChild('networkContainer', { static: false }) networkContainer!: ElementRef;
  
  templateId!: number;
  template: WorkflowTemplateDetailResponse | null = null;
  isLoading = false;
  errorMessage = '';
  private networkInstance: Network | null = null;

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
        
        // Inicijalizujemo graf sa malim zakašnjenjem da se DOM iscrta
        setTimeout(() => this.initWorkflowGraph(), 50);
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

private initWorkflowGraph(): void {
    if (!this.template || !this.networkContainer?.nativeElement) return;

    // Čvorovi (Ovo je radilo)
    const nodesArray: any[] = this.template.states.map(state => {
      let color = '#e3f2fd';
      let borderWidth = 1;
      if (state.initial) { color = '#e8f5e9'; borderWidth = 2; }
      else if (state.finalState) { color = '#ffebee'; borderWidth = 2; }

      return {
        id: state.id,
        label: state.name,
        shape: 'box',
        margin: 12,
        font: { size: 14, color: '#111111', face: 'Inter, sans-serif', bold: true },
        color: { background: color, border: state.initial ? '#2e7d32' : state.finalState ? '#c62828' : '#1e88e5' },
        borderWidth: borderWidth,
        shapeProperties: { borderRadius: 6 }
      };
    });

    // Grane (Vraćamo se na proverenu strukturu, dodajemo tooltip)
    const edgesArray: any[] = this.template.transitions.map(trans => {
      const condLabels = trans.conditions.map(c => (c.necessary ? '★ ' : '') + c.label).join(', ');
      
      return {
        from: trans.sourceStateId,
        to: trans.targetStateId,
        label: trans.label, // Fokus na labeli
        title: condLabels || 'No conditions', // Tooltip se vidi kad zadržiš miša
        arrows: 'to',
        font: { size: 12, align: 'top', color: '#333', background: '#ffffff' },
        smooth: { enabled: true, type: 'cubicBezier', roundness: 0.3 }
      };
    });

    const data = {
      nodes: new DataSet(nodesArray),
      edges: new DataSet(edgesArray)
    };

    // PROVERENA KONFIGURACIJA
    const options: any = {
      physics: {
        enabled: true,
        barnesHut: { gravitationalConstant: -2000, springLength: 150 }
      }
    };

    // Ako je stara instanca tu, uništi je da ne pravi haos
    if (this.networkInstance) {
      this.networkInstance.destroy();
    }

    // @ts-ignore
    this.networkInstance = new Network(this.networkContainer.nativeElement, data, options);
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