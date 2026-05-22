import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { SegmentService } from '../../../core/services/segment.service';
import { BinaService } from '../../../core/services/bina.service';
import { Segment, StageSegment } from '../../../core/models/segment.model';
import { Stage } from '../../../core/models/bina.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-segmenti',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './segmenti.component.html',
  styleUrls: ['./segmenti.component.css']
})
export class SegmentiComponent implements OnInit {
  private authService = inject(AuthService);
  private segmentService = inject(SegmentService);
  private binaService = inject(BinaService);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);

  currentUser: User | null = null;
  errorMessage = '';

  // Festival segments
  festivalSegments: Segment[] = [];
  showSegmentForm = false;
  segmentForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]]
  });

  // Stages and assignments
  stages: Stage[] = [];
  selectedStage: Stage | null = null;
  stageSegments: StageSegment[] = [];
  showAssignForm = false;
  editingAssignment: StageSegment | null = null;

  assignForm = this.fb.group({
    segmentId: [null as number | null, Validators.required],
    capacity: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  editForm = this.fb.group({
    capacity: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  get festivalId(): number {
    return this.currentUser?.assignment?.festivalId ?? 0;
  }

  get availableSegmentsForAssign(): Segment[] {
    const assigned = new Set(this.stageSegments.map(ss => ss.segmentId));
    return this.festivalSegments.filter(s => !assigned.has(s.segmentId));
  }

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => {
      this.currentUser = user;
      if (user?.assignment?.festivalId) {
        this.loadFestivalSegments();
        this.binaService.getAll().pipe(
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({ next: s => this.stages = s });
      }
    });
  }

  loadFestivalSegments(): void {
    this.segmentService.getFestivalSegments(this.festivalId).subscribe({
      next: data => this.festivalSegments = data,
      error: () => this.errorMessage = 'Error loading segments.'
    });
  }

  createSegment(): void {
    if (this.segmentForm.invalid) { this.segmentForm.markAllAsTouched(); return; }
    this.segmentService.createSegment(this.festivalId, this.segmentForm.value.name!).subscribe({
      next: () => {
        this.segmentForm.reset();
        this.showSegmentForm = false;
        this.loadFestivalSegments();
      },
      error: (err) => this.errorMessage = err.error?.message || 'Error creating segment.'
    });
  }

  deleteSegment(s: Segment): void {
    if (!confirm(`Delete segment "${s.name}"? This will also remove all stage assignments for this segment.`)) return;
    this.segmentService.deleteSegment(this.festivalId, s.segmentId).subscribe({
      next: () => {
        this.loadFestivalSegments();
        if (this.selectedStage) this.loadStageSegments(this.selectedStage);
      },
      error: () => this.errorMessage = 'Error deleting segment.'
    });
  }

  selectStage(stage: Stage): void {
    this.selectedStage = stage;
    this.showAssignForm = false;
    this.editingAssignment = null;
    this.loadStageSegments(stage);
  }

  loadStageSegments(stage: Stage): void {
    this.segmentService.getStageSegments(stage.stageId).subscribe({
      next: data => this.stageSegments = data,
      error: () => this.errorMessage = 'Error loading stage segments.'
    });
  }

  openAssignForm(): void {
    this.showAssignForm = true;
    this.editingAssignment = null;
    this.assignForm.reset();
  }

  cancelAssignForm(): void {
    this.showAssignForm = false;
    this.assignForm.reset();
  }

  assign(): void {
    if (this.assignForm.invalid || !this.selectedStage) { this.assignForm.markAllAsTouched(); return; }
    const { segmentId, capacity } = this.assignForm.value;
    this.segmentService.assignSegment(this.selectedStage.stageId, segmentId!, capacity!).subscribe({
      next: () => {
        this.cancelAssignForm();
        this.loadStageSegments(this.selectedStage!);
      },
      error: (err) => this.errorMessage = err.error?.message || 'Error assigning segment.'
    });
  }

  openEdit(ss: StageSegment): void {
    this.editingAssignment = ss;
    this.showAssignForm = false;
    this.editForm.patchValue({ capacity: ss.capacity });
  }

  cancelEdit(): void {
    this.editingAssignment = null;
    this.editForm.reset();
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.selectedStage || !this.editingAssignment) return;
    this.segmentService.updateAssignment(
      this.selectedStage.stageId,
      this.editingAssignment.segmentId,
      this.editForm.value.capacity!
    ).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadStageSegments(this.selectedStage!);
      },
      error: () => this.errorMessage = 'Error updating capacity.'
    });
  }

  removeFromStage(ss: StageSegment): void {
    if (!confirm(`Remove segment "${ss.segmentName}" from this stage?`)) return;
    this.segmentService.removeFromStage(this.selectedStage!.stageId, ss.segmentId).subscribe({
      next: () => this.loadStageSegments(this.selectedStage!),
      error: () => this.errorMessage = 'Error removing segment.'
    });
  }
}
