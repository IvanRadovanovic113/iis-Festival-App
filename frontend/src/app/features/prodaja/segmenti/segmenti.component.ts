import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { SegmentService } from '../../../core/services/segment.service';
import { BinaService } from '../../../core/services/bina.service';
import { Segment, BinaSegment } from '../../../core/models/segment.model';
import { Bina } from '../../../core/models/bina.model';
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
  private fb = inject(FormBuilder);

  currentUser: User | null = null;
  errorMessage = '';

  // Festival segmenti
  festivalSegmenti: Segment[] = [];
  showSegmentForm = false;
  segmentForm = this.fb.group({
    naziv: ['', [Validators.required, Validators.minLength(2)]]
  });

  // Bine i dodela
  bine: Bina[] = [];
  selectedBina: Bina | null = null;
  binaSegmenti: BinaSegment[] = [];
  showAssignForm = false;
  editingAssignment: BinaSegment | null = null;

  assignForm = this.fb.group({
    segmentId: [null as number | null, Validators.required],
    kapacitet: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  editForm = this.fb.group({
    kapacitet: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  get festivalId(): number {
    return this.currentUser?.assignment?.festivalId ?? 0;
  }

  get availableSegmentsForAssign(): Segment[] {
    const assigned = new Set(this.binaSegmenti.map(bs => bs.segmentId));
    return this.festivalSegmenti.filter(s => !assigned.has(s.segmentId));
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (user?.assignment?.festivalId) {
        this.loadFestivalSegmenti();
      }
    });
    this.binaService.getAll().subscribe({ next: b => this.bine = b });
  }

  loadFestivalSegmenti(): void {
    this.segmentService.getFestivalSegments(this.festivalId).subscribe({
      next: data => this.festivalSegmenti = data,
      error: () => this.errorMessage = 'Greška pri učitavanju segmenata.'
    });
  }

  createSegment(): void {
    if (this.segmentForm.invalid) { this.segmentForm.markAllAsTouched(); return; }
    this.segmentService.createSegment(this.festivalId, this.segmentForm.value.naziv!).subscribe({
      next: () => {
        this.segmentForm.reset();
        this.showSegmentForm = false;
        this.loadFestivalSegmenti();
      },
      error: (err) => this.errorMessage = err.error?.message || 'Greška pri kreiranju segmenta.'
    });
  }

  deleteSegment(s: Segment): void {
    if (!confirm(`Obrisati segment "${s.naziv}"? Ovo će ukloniti i sve dodele ovog segmenta binama.`)) return;
    this.segmentService.deleteSegment(this.festivalId, s.segmentId).subscribe({
      next: () => {
        this.loadFestivalSegmenti();
        if (this.selectedBina) this.loadBinaSegmenti(this.selectedBina);
      },
      error: () => this.errorMessage = 'Greška pri brisanju segmenta.'
    });
  }

  selectBina(bina: Bina): void {
    this.selectedBina = bina;
    this.showAssignForm = false;
    this.editingAssignment = null;
    this.loadBinaSegmenti(bina);
  }

  loadBinaSegmenti(bina: Bina): void {
    this.segmentService.getStageSegments(bina.binaId).subscribe({
      next: data => this.binaSegmenti = data,
      error: () => this.errorMessage = 'Greška pri učitavanju segmenata bine.'
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
    if (this.assignForm.invalid || !this.selectedBina) { this.assignForm.markAllAsTouched(); return; }
    const { segmentId, kapacitet } = this.assignForm.value;
    this.segmentService.assignSegment(this.selectedBina.binaId, segmentId!, kapacitet!).subscribe({
      next: () => {
        this.cancelAssignForm();
        this.loadBinaSegmenti(this.selectedBina!);
      },
      error: (err) => this.errorMessage = err.error?.message || 'Greška pri dodeli segmenta.'
    });
  }

  openEdit(bs: BinaSegment): void {
    this.editingAssignment = bs;
    this.showAssignForm = false;
    this.editForm.patchValue({ kapacitet: bs.kapacitet });
  }

  cancelEdit(): void {
    this.editingAssignment = null;
    this.editForm.reset();
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.selectedBina || !this.editingAssignment) return;
    this.segmentService.updateAssignment(
      this.selectedBina.binaId,
      this.editingAssignment.segmentId,
      this.editForm.value.kapacitet!
    ).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadBinaSegmenti(this.selectedBina!);
      },
      error: () => this.errorMessage = 'Greška pri izmeni kapaciteta.'
    });
  }

  removeFromStage(bs: BinaSegment): void {
    if (!confirm(`Ukloniti segment "${bs.segmentNaziv}" sa bine?`)) return;
    this.segmentService.removeFromStage(this.selectedBina!.binaId, bs.segmentId).subscribe({
      next: () => this.loadBinaSegmenti(this.selectedBina!),
      error: () => this.errorMessage = 'Greška pri uklanjanju segmenta.'
    });
  }
}
