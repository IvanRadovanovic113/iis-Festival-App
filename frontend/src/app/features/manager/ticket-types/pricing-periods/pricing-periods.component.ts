import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { PricingPeriodService } from '../../../../core/services/pricing-period.service';
import { OcekivanaProdajaService } from '../../../../core/services/ocekivana-prodaja.service';
import { TicketType } from '../../../../core/models/ticket-type.model';
import { PricingPeriod, PricingPeriodRequest } from '../../../../core/models/pricing-period.model';
import { OcekivanaProdajaRequest } from '../../../../core/models/ocekivana-prodaja.model';

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
  private ocekivanaService = inject(OcekivanaProdajaService);
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

  // DP config per period: periodId → expanded state
  dpExpandedPeriodId: number | null = null;
  dpSubmitting = false;
  dpError = '';

  form = this.fb.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    basePrice: [null as number | null, [Validators.required, Validators.min(0.01)]],
    minPrice: [null as number | null, [Validators.required, Validators.min(0.01)]],
    dynamicPricingActive: [false]
  });

  dpForm = this.fb.group({
    brojKarata: [null as number | null, [Validators.required, Validators.min(1)]],
    brojSati: [null as number | null, [Validators.required, Validators.min(1)]],
    agresivnost: [null as number | null, [Validators.required, Validators.min(0.01), Validators.max(1)]],
    intervalMinuti: [null as number | null, [Validators.required, Validators.min(1)]],
    scarcityPragNizak: [null as number | null, [Validators.required, Validators.min(1), Validators.max(99)]],
    scarcityPragVisok: [null as number | null, [Validators.required, Validators.min(1), Validators.max(99)]],
    scarcityMultiplikatorNizak: [null as number | null, [Validators.required, Validators.min(1)]],
    scarcityMultiplikatorVisok: [null as number | null, [Validators.required, Validators.min(1)]]
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

  toggleDpConfig(period: PricingPeriod): void {
    if (this.dpExpandedPeriodId === period.pricingPeriodId) {
      this.dpExpandedPeriodId = null;
      return;
    }
    this.dpExpandedPeriodId = period.pricingPeriodId;
    this.dpError = '';
    const op = period.ocekivanaProdaja;
    this.dpForm.patchValue({
      brojKarata: op?.brojKarata ?? null,
      brojSati: op?.brojSati ?? null,
      agresivnost: op?.agresivnost ?? null,
      intervalMinuti: op?.intervalMinuti ?? null,
      scarcityPragNizak: op?.scarcityPragNizak ?? null,
      scarcityPragVisok: op?.scarcityPragVisok ?? null,
      scarcityMultiplikatorNizak: op?.scarcityMultiplikatorNizak ?? null,
      scarcityMultiplikatorVisok: op?.scarcityMultiplikatorVisok ?? null
    });
  }

  saveDpConfig(period: PricingPeriod): void {
    if (this.dpForm.invalid) {
      this.dpForm.markAllAsTouched();
      return;
    }
    this.dpSubmitting = true;
    this.dpError = '';

    const request: OcekivanaProdajaRequest = {
      brojKarata: this.dpForm.value.brojKarata!,
      brojSati: this.dpForm.value.brojSati!,
      agresivnost: this.dpForm.value.agresivnost!,
      intervalMinuti: this.dpForm.value.intervalMinuti!,
      scarcityPragNizak: this.dpForm.value.scarcityPragNizak!,
      scarcityPragVisok: this.dpForm.value.scarcityPragVisok!,
      scarcityMultiplikatorNizak: this.dpForm.value.scarcityMultiplikatorNizak!,
      scarcityMultiplikatorVisok: this.dpForm.value.scarcityMultiplikatorVisok!
    };

    this.ocekivanaService.upsert(period.pricingPeriodId, request).subscribe({
      next: saved => {
        this.periods = this.periods.map(p =>
          p.pricingPeriodId === period.pricingPeriodId
            ? { ...p, ocekivanaProdaja: saved }
            : p
        );
        this.dpExpandedPeriodId = null;
        this.dpSubmitting = false;
      },
      error: err => {
        this.dpError = err.error?.message || 'Error saving configuration.';
        this.dpSubmitting = false;
      }
    });
  }

  cancelDpConfig(): void {
    this.dpExpandedPeriodId = null;
    this.dpForm.reset();
    this.dpError = '';
  }
}
