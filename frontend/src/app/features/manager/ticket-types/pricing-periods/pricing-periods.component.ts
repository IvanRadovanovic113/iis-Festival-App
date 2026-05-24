import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { PricingPeriodService } from '../../../../core/services/pricing-period.service';
import { TicketType } from '../../../../core/models/ticket-type.model';
import { PricingPeriod, PricingPeriodRequest } from '../../../../core/models/pricing-period.model';

@Component({
  selector: 'app-pricing-periods',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './pricing-periods.component.html',
  styleUrls: ['./pricing-periods.component.css']
})
export class PricingPeriodsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private ticketTypeService = inject(TicketTypeService);
  private pricingPeriodService = inject(PricingPeriodService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  ticketTypeId!: number;
  ticketType: TicketType | null = null;
  periods: PricingPeriod[] = [];
  errorMessage = '';
  editingId: number | null = null;
  showForm = false;
  submitting = false;
  togglingPricing = false;

  form = this.fb.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    basePrice: [null as number | null, [Validators.required, Validators.min(0.01)]],
    minPrice: [null as number | null, [Validators.required, Validators.min(0.01)]],
    dynamicPricingActive: [false]
  });

  get hasDynamicPeriodWarning(): boolean {
    return Boolean(this.ticketType?.dynamicPricingActive) &&
      !this.periods.some(p => p.dynamicPricingActive);
  }

  get startDate() { return this.form.get('startDate')!; }
  get endDate() { return this.form.get('endDate')!; }
  get basePrice() { return this.form.get('basePrice')!; }
  get minPrice() { return this.form.get('minPrice')!; }

  ngOnInit(): void {
    this.ticketTypeId = +this.route.snapshot.paramMap.get('id')!;
    this.loadTicketType();
    this.loadPeriods();
  }

  private loadTicketType(): void {
    this.ticketTypeService.getById(this.ticketTypeId).subscribe({
      next: tt => this.ticketType = tt,
      error: () => this.errorMessage = 'Error loading ticket type.'
    });
  }

  private loadPeriods(): void {
    this.pricingPeriodService.getAll(this.ticketTypeId).subscribe({
      next: data => this.periods = data,
      error: () => this.errorMessage = 'Error loading pricing periods.'
    });
  }

  openAddForm(): void {
    this.editingId = null;
    this.form.reset({ dynamicPricingActive: false });
    this.showForm = true;
    this.errorMessage = '';
  }

  openEditForm(period: PricingPeriod): void {
    this.editingId = period.pricingPeriodId;
    this.form.patchValue({
      startDate: period.startDate,
      endDate: period.endDate,
      basePrice: period.basePrice,
      minPrice: period.minPrice,
      dynamicPricingActive: period.dynamicPricingActive
    });
    this.showForm = true;
    this.errorMessage = '';
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.form.reset();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.errorMessage = '';

    const request: PricingPeriodRequest = {
      startDate: this.form.value.startDate!,
      endDate: this.form.value.endDate!,
      basePrice: this.form.value.basePrice!,
      minPrice: this.form.value.minPrice!,
      dynamicPricingActive: this.form.value.dynamicPricingActive ?? false
    };

    const op = this.editingId !== null
      ? this.pricingPeriodService.update(this.ticketTypeId, this.editingId, request)
      : this.pricingPeriodService.create(this.ticketTypeId, request);

    op.subscribe({
      next: saved => {
        if (this.editingId !== null) {
          this.periods = this.periods.map(p => p.pricingPeriodId === saved.pricingPeriodId ? saved : p);
        } else {
          this.periods = [...this.periods, saved].sort(
            (a, b) => a.startDate.localeCompare(b.startDate)
          );
        }
        this.showForm = false;
        this.editingId = null;
        this.submitting = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error saving pricing period.';
        this.submitting = false;
      }
    });
  }

  deletePeriod(period: PricingPeriod): void {
    if (!confirm(`Delete pricing period ${period.startDate} – ${period.endDate}?`)) return;
    this.pricingPeriodService.delete(this.ticketTypeId, period.pricingPeriodId).subscribe({
      next: () => this.periods = this.periods.filter(p => p.pricingPeriodId !== period.pricingPeriodId),
      error: err => this.errorMessage = err.error?.message || 'Error deleting period.'
    });
  }

  toggleGlobalDynamicPricing(): void {
    if (!this.ticketType) return;
    this.togglingPricing = true;
    const newValue = !this.ticketType.dynamicPricingActive;
    this.ticketTypeService.toggleDynamicPricing(this.ticketTypeId, newValue).subscribe({
      next: updated => {
        this.ticketType = updated;
        this.togglingPricing = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error toggling dynamic pricing.';
        this.togglingPricing = false;
      }
    });
  }
}
