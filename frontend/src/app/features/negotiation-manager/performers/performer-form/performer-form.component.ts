import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PerformerService } from '../../../../core/services/performer.service';
import { PerformerType } from '../../../../core/models/performer.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-performer-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './performer-form.component.html',
  styleUrls: ['./performer-form.component.css']
})
export class PerformerFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private performerService = inject(PerformerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  performerForm!: FormGroup;
  isEditMode = false;
  performerId: number | null = null;
  errorMessage: string | null = null;

  // Izlaganje enuma za HTML template
  PerformerType = PerformerType;

  ngOnInit(): void {
    this.initForm();
    this.checkForEditMode();
  }

  private initForm(): void {
    this.performerForm = this.fb.group({
      stageName: ['', [Validators.required, Validators.maxLength(100)]],
      firstName: [''],
      lastName: [''],
      genre: ['', [Validators.required]],
      popularity: [50, [Validators.required, Validators.min(0), Validators.max(100)]],
      averageDurationMinutes: [60, [Validators.required, Validators.min(1)]],
      countryOfOrigin: ['', [Validators.required]],
      performerType: [PerformerType.SOLO, [Validators.required]],
      numberOfMembers: [1, [Validators.required, Validators.min(1)]],
      bio: ['', [Validators.maxLength(1000)]]
    });

    // Automatsko prilagođavanje broja članova u zavisnosti od tipa izvođača
    this.performerForm.get('performerType')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(type => {
        const membersControl = this.performerForm.get('numberOfMembers');
        if (type === PerformerType.SOLO) {
          membersControl?.setValue(1);
          membersControl?.disable();
        } else {
          membersControl?.enable();
          if (membersControl?.value === 1) {
            membersControl?.setValue(2); // Ako je bend, logično je da ima bar 2 člana
          }
        }
      });
  }

  private checkForEditMode(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const idParam = params.get('id');
        if (idParam) {
          this.isEditMode = true;
          this.performerId = +idParam;
          this.loadPerformerData(this.performerId);
        }
      });
  }

  private loadPerformerData(id: number): void {
    this.performerService.getPerformerById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (performer) => {
          // patchValue puni formu podacima sa backenda
          this.performerForm.patchValue(performer);
          // Eksplicitno aktiviramo/deaktiviramo polje za članove na osnovu učitanog tipa
          if (performer.performerType === PerformerType.SOLO) {
            this.performerForm.get('numberOfMembers')?.disable();
          }
        },
        error: (err) => {
          this.errorMessage = 'Failed to load performer data. It might not exist.';
          console.error(err);
        }
      });
  }

  onSubmit(): void {
    if (this.performerForm.invalid) {
      this.performerForm.markAllAsTouched();
      return;
    }

    // getRawValue uzima podatke uključujući i disabled polja (poput numberOfMembers za solo umetnike)
    const requestData = this.performerForm.getRawValue();

    if (this.isEditMode && this.performerId) {
      this.performerService.updatePerformer(this.performerId, requestData)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.router.navigate(['/negotiation-manager/performers', this.performerId]),
          error: (err) => this.handleError(err)
        });
    } else {
      this.performerService.createPerformer(requestData)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.router.navigate(['/negotiation-manager/performers']),
          error: (err) => this.handleError(err)
        });
    }
  }

  private handleError(error: any): void {
    this.errorMessage = error?.error?.message || 'An error occurred while saving the performer.';
    console.error(error);
  }
}