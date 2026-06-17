import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PerformerSchedulingService } from '../../../../core/services/performer-scheduling.service';
import { BinaService } from '../../../../core/services/bina.service';
import { PerformerSchedulingItem } from '../../../../core/models/contract.model';
import { Stage } from '../../../../core/models/bina.model';

@Component({
  selector: 'app-performer-scheduling-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './performer-scheduling-list.component.html',
  styleUrls: ['./performer-scheduling-list.component.css']
})
export class PerformerSchedulingListComponent implements OnInit {
  private schedulingService = inject(PerformerSchedulingService);
  private binaService = inject(BinaService);

  items: PerformerSchedulingItem[] = [];
  stages: Stage[] = [];
  errorMessage = '';
  selectedStageIds: Record<number, number> = {};
  submittingId: number | null = null;

  ngOnInit(): void {
    this.loadItems();
    this.loadStages();
  }

  private loadItems(): void {
    this.schedulingService.getAll().subscribe({
      next: data => this.items = data,
      error: () => this.errorMessage = 'Error loading scheduling data.'
    });
  }

  private loadStages(): void {
    this.binaService.getAll().subscribe({
      next: data => this.stages = data,
      error: () => this.errorMessage = 'Error loading stages.'
    });
  }

  assign(item: PerformerSchedulingItem): void {
    const stageId = this.selectedStageIds[item.contractId];
    if (!stageId) return;
    this.submittingId = item.contractId;
    this.errorMessage = '';
    this.schedulingService.assignStage(item.contractId, stageId).subscribe({
      next: updated => {
        this.items = this.items.map(i => i.contractId === updated.contractId ? updated : i);
        delete this.selectedStageIds[item.contractId];
        this.submittingId = null;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error assigning stage.';
        this.submittingId = null;
      }
    });
  }

  remove(item: PerformerSchedulingItem): void {
    this.submittingId = item.contractId;
    this.errorMessage = '';
    this.schedulingService.removeStage(item.contractId).subscribe({
      next: () => {
        this.items = this.items.map(i =>
          i.contractId === item.contractId
            ? { ...i, stageId: null, stageName: null, schedulingStatus: 'NOT_ASSIGNED' }
            : i
        );
        this.submittingId = null;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error removing stage.';
        this.submittingId = null;
      }
    });
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'NOT_ASSIGNED': return 'Not Assigned';
      case 'STAGE_ASSIGNED': return 'Stage Assigned';
      case 'TIME_ASSIGNED': return 'Time Assigned';
      default: return status;
    }
  }
}
