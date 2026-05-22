import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FestivalService } from '../../../core/services/festival.service';
import { AuthService } from '../../../core/services/auth.service';
import { FestivalStatus, FESTIVAL_STATUS_LABELS } from '../../../core/models/festival.model';

function dateRangeValidator(control: AbstractControl): ValidationErrors | null {
  const start = control.get('startDate')?.value;
  const end = control.get('endDate')?.value;
  if (start && end && end <= start) {
    return { dateRange: true };
  }
  return null;
}

@Component({
  selector: 'app-festival-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './festival-form.component.html',
  styleUrls: ['./festival-form.component.css']
})
export class FestivalFormComponent {
  private fb = inject(FormBuilder);
  private festivalService = inject(FestivalService);
  private router = inject(Router);
  private authService = inject(AuthService);

  statuses: FestivalStatus[] = ['ACTIVE', 'INACTIVE', 'UPCOMING', 'COMPLETED', 'CANCELLED'];
  statusLabels = FESTIVAL_STATUS_LABELS;
  submitting = false;
  errorMessage = '';

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    location: ['', [Validators.required, Validators.minLength(2)]],
    status: ['' as FestivalStatus, Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  }, { validators: dateRangeValidator });

  get name() { return this.form.get('name')!; }
  get location() { return this.form.get('location')!; }
  get status() { return this.form.get('status')!; }
  get startDate() { return this.form.get('startDate')!; }
  get endDate() { return this.form.get('endDate')!; }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.errorMessage = '';
    this.festivalService.create(this.form.value as any).subscribe({
      next: () => this.router.navigate(['/admin/festivals']),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error creating festival.';
        this.submitting = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
