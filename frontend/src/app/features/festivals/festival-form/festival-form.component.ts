import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FestivalService } from '../../../core/services/festival.service';
import { AuthService } from '../../../core/services/auth.service';
import { FestivalStatus, FESTIVAL_STATUS_LABELS } from '../../../core/models/festival.model';

function dateRangeValidator(control: AbstractControl): ValidationErrors | null {
  const start = control.get('datumPocetka')?.value;
  const end = control.get('datumZavrsetka')?.value;
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

  statusi: FestivalStatus[] = ['AKTIVAN', 'NEAKTIVAN', 'NADOLAZECI', 'ZAVRSEN', 'OTKAZAN'];
  statusLabels = FESTIVAL_STATUS_LABELS;
  submitting = false;
  errorMessage = '';

  form = this.fb.group({
    naziv: ['', [Validators.required, Validators.minLength(2)]],
    lokacija: ['', [Validators.required, Validators.minLength(2)]],
    status: ['' as FestivalStatus, Validators.required],
    datumPocetka: ['', Validators.required],
    datumZavrsetka: ['', Validators.required]
  }, { validators: dateRangeValidator });

  get naziv() { return this.form.get('naziv')!; }
  get lokacija() { return this.form.get('lokacija')!; }
  get status() { return this.form.get('status')!; }
  get datumPocetka() { return this.form.get('datumPocetka')!; }
  get datumZavrsetka() { return this.form.get('datumZavrsetka')!; }

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
        this.errorMessage = err.error?.message || 'Greška pri kreiranju festivala.';
        this.submitting = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
