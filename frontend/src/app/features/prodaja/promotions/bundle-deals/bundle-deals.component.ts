import { Component, OnInit, Input, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { BundleDealService } from '../../../../core/services/bundle-deal.service';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { BundleDeal, BundleDealRequest } from '../../../../core/models/bundle-deal.model';
import { TicketType } from '../../../../core/models/ticket-type.model';

@Component({
  selector: 'app-bundle-deals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './bundle-deals.component.html',
  styleUrls: ['./bundle-deals.component.css']
})
export class BundleDealsComponent implements OnInit {
  @Input() festivalId!: number;

  private bundleDealService = inject(BundleDealService);
  private ticketTypeService = inject(TicketTypeService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  deals: BundleDeal[] = [];
  ticketTypes: TicketType[] = [];
  errorMessage = '';
  editingId: number | null = null;
  showForm = false;
  submitting = false;

  form = this.fb.group({
    ticketTypeId: [null as number | null, Validators.required],
    kupiKarata: [null as number | null, [Validators.required, Validators.min(1)]],
    dobijaKarata: [null as number | null, [Validators.required, Validators.min(1)]],
    vaziOd: ['', Validators.required],
    vaziDo: ['', Validators.required],
    dostupnoAkcija: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  get ticketTypeId() { return this.form.get('ticketTypeId')!; }
  get kupiKarata() { return this.form.get('kupiKarata')!; }
  get dobijaKarata() { return this.form.get('dobijaKarata')!; }
  get vaziOd() { return this.form.get('vaziOd')!; }
  get vaziDo() { return this.form.get('vaziDo')!; }
  get dostupnoAkcija() { return this.form.get('dostupnoAkcija')!; }

  ngOnInit(): void {
    this.loadDeals();
    this.loadTicketTypes();
  }

  private loadDeals(): void {
    this.bundleDealService.getAll(this.festivalId).subscribe({
      next: data => this.deals = data,
      error: () => this.errorMessage = 'Error loading bundle deals.'
    });
  }

  private loadTicketTypes(): void {
    this.ticketTypeService.getAll(this.festivalId).subscribe({
      next: data => this.ticketTypes = data,
      error: () => {}
    });
  }

  dealTitle(deal: BundleDeal): string {
    return `Buy ${deal.kupiKarata}, Get ${deal.dobijaKarata} Free`;
  }

  formatUses(deal: BundleDeal): string {
    return `${deal.usedCount}/${deal.dostupnoAkcija} used`;
  }

  openAddForm(): void {
    this.editingId = null;
    this.form.reset();
    this.showForm = true;
    this.errorMessage = '';
  }

  openEditForm(deal: BundleDeal): void {
    this.editingId = deal.akcijaId;
    this.form.patchValue({
      ticketTypeId: deal.ticketTypeId,
      kupiKarata: deal.kupiKarata,
      dobijaKarata: deal.dobijaKarata,
      vaziOd: deal.vaziOd,
      vaziDo: deal.vaziDo,
      dostupnoAkcija: deal.dostupnoAkcija
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

    const v = this.form.value;
    const request: BundleDealRequest = {
      ticketTypeId: v.ticketTypeId!,
      kupiKarata: v.kupiKarata!,
      dobijaKarata: v.dobijaKarata!,
      vaziOd: v.vaziOd!,
      vaziDo: v.vaziDo!,
      dostupnoAkcija: v.dostupnoAkcija!
    };

    const op = this.editingId !== null
      ? this.bundleDealService.update(this.editingId, request)
      : this.bundleDealService.create(this.festivalId, request);

    op.subscribe({
      next: saved => {
        if (this.editingId !== null) {
          this.deals = this.deals.map(d => d.akcijaId === saved.akcijaId ? saved : d);
        } else {
          this.deals = [saved, ...this.deals];
        }
        this.showForm = false;
        this.editingId = null;
        this.submitting = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error saving bundle deal.';
        this.submitting = false;
      }
    });
  }

  toggleActive(deal: BundleDeal): void {
    this.bundleDealService.toggleActive(deal.akcijaId).subscribe({
      next: updated => this.deals = this.deals.map(d => d.akcijaId === updated.akcijaId ? updated : d),
      error: err => this.errorMessage = err.error?.message || 'Error updating bundle deal.'
    });
  }

  delete(deal: BundleDeal): void {
    if (!confirm(`Delete bundle deal "${this.dealTitle(deal)}"?`)) return;
    this.bundleDealService.delete(deal.akcijaId).subscribe({
      next: () => this.deals = this.deals.filter(d => d.akcijaId !== deal.akcijaId),
      error: err => this.errorMessage = err.error?.message || 'Error deleting bundle deal.'
    });
  }
}
