import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BinaService } from '../../../core/services/bina.service';
import { AuthService } from '../../../core/services/auth.service';
import { Stage, StageRequest } from '../../../core/models/bina.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-stage-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './stage-list.component.html',
  styleUrls: ['./stage-list.component.css']
})
export class StageListComponent implements OnInit {
  private stageService = inject(BinaService);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);

  stages: Stage[] = [];
  currentUser: User | null = null;
  errorMessage = '';

  formMode: 'create' | 'edit' | null = null;
  selectedStage: Stage | null = null;

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    capacity: [null as number | null, [Validators.required, Validators.min(1)]],
    location: ['', [Validators.required, Validators.minLength(2)]]
  });

  get name() { return this.form.get('name')!; }
  get capacity() { return this.form.get('capacity')!; }
  get location() { return this.form.get('location')!; }

  get festivalName(): string {
    return this.currentUser?.assignment?.festivalName ?? '';
  }

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => this.currentUser = user);
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.stageService.getAll().subscribe({
      next: data => this.stages = data,
      error: () => this.errorMessage = 'Error loading stages.'
    });
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedStage = null;
    this.form.reset();
  }

  openEdit(stage: Stage): void {
    this.formMode = 'edit';
    this.selectedStage = stage;
    this.form.patchValue({
      name: stage.name,
      capacity: stage.capacity,
      location: stage.location
    });
  }

  cancelForm(): void {
    this.formMode = null;
    this.selectedStage = null;
    this.form.reset();
    this.errorMessage = '';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.value as StageRequest;
    const op = this.formMode === 'edit' && this.selectedStage
      ? this.stageService.update(this.selectedStage.stageId, request)
      : this.stageService.create(request);

    op.subscribe({
      next: () => {
        this.cancelForm();
        this.load();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error saving stage.';
      }
    });
  }

  delete(stage: Stage): void {
    if (!confirm(`Are you sure you want to delete stage "${stage.name}"?`)) return;
    this.stageService.delete(stage.stageId).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage = 'Error deleting stage.'
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
