import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PromoCodeService } from '../../../core/services/promo-code.service';
import { PromoCode, PromoCodeRequest } from '../../../core/models/promo-code.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './promotions.component.html',
  styleUrls: ['./promotions.component.css']
})
export class PromotionsComponent implements OnInit {
  private authService = inject(AuthService);
  private promoCodeService = inject(PromoCodeService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  currentUser: User | null = null;
  promoCodes: PromoCode[] = [];
  errorMessage = '';
  editingId: number | null = null;
  showForm = false;
  submitting = false;
  unlimited = false;

  form = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[A-Za-z0-9_-]+$/)]],
    discountPercent: [null as number | null, [Validators.required, Validators.min(1), Validators.max(100)]],
    validFrom: ['', Validators.required],
    validTo: ['', Validators.required],
    maxUses: [null as number | null, Validators.min(1)]
  });

  get code() { return this.form.get('code')!; }
  get discountPercent() { return this.form.get('discountPercent')!; }
  get validFrom() { return this.form.get('validFrom')!; }
  get validTo() { return this.form.get('validTo')!; }
  get maxUses() { return this.form.get('maxUses')!; }

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => {
      this.currentUser = user;
      if (user?.assignment?.festivalId) {
        this.loadPromoCodes(user.assignment.festivalId);
      }
    });
  }

  private loadPromoCodes(festivalId: number): void {
    this.promoCodeService.getAll(festivalId).subscribe({
      next: data => this.promoCodes = data,
      error: () => this.errorMessage = 'Error loading promo codes.'
    });
  }

  openAddForm(): void {
    this.editingId = null;
    this.unlimited = false;
    this.form.reset();
    this.maxUses.enable();
    this.showForm = true;
    this.errorMessage = '';
  }

  openEditForm(promo: PromoCode): void {
    this.editingId = promo.promoCodeId;
    this.unlimited = promo.maxUses === null;
    this.form.patchValue({
      code: promo.code,
      discountPercent: promo.discountPercent,
      validFrom: promo.validFrom,
      validTo: promo.validTo,
      maxUses: promo.maxUses
    });
    if (this.unlimited) {
      this.maxUses.disable();
    } else {
      this.maxUses.enable();
    }
    this.showForm = true;
    this.errorMessage = '';
  }

  toggleUnlimited(): void {
    this.unlimited = !this.unlimited;
    if (this.unlimited) {
      this.maxUses.setValue(null);
      this.maxUses.disable();
    } else {
      this.maxUses.enable();
    }
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.unlimited = false;
    this.maxUses.enable();
    this.form.reset();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.errorMessage = '';

    const request: PromoCodeRequest = {
      code: this.form.value.code!,
      discountPercent: this.form.value.discountPercent!,
      validFrom: this.form.value.validFrom!,
      validTo: this.form.value.validTo!,
      maxUses: this.unlimited ? null : (this.form.value.maxUses ?? null)
    };

    const festivalId = this.currentUser!.assignment!.festivalId;
    const op = this.editingId !== null
      ? this.promoCodeService.update(this.editingId, request)
      : this.promoCodeService.create(festivalId, request);

    op.subscribe({
      next: saved => {
        if (this.editingId !== null) {
          this.promoCodes = this.promoCodes.map(p => p.promoCodeId === saved.promoCodeId ? saved : p);
        } else {
          this.promoCodes = [saved, ...this.promoCodes];
        }
        this.showForm = false;
        this.editingId = null;
        this.unlimited = false;
        this.maxUses.enable();
        this.submitting = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Error saving promo code.';
        this.submitting = false;
      }
    });
  }

  toggleActive(promo: PromoCode): void {
    this.promoCodeService.toggleActive(promo.promoCodeId).subscribe({
      next: updated => this.promoCodes = this.promoCodes.map(p => p.promoCodeId === updated.promoCodeId ? updated : p),
      error: err => this.errorMessage = err.error?.message || 'Error updating promo code.'
    });
  }

  delete(promo: PromoCode): void {
    if (!confirm(`Delete promo code "${promo.code}"?`)) return;
    this.promoCodeService.delete(promo.promoCodeId).subscribe({
      next: () => this.promoCodes = this.promoCodes.filter(p => p.promoCodeId !== promo.promoCodeId),
      error: err => this.errorMessage = err.error?.message || 'Error deleting promo code.'
    });
  }

  formatUses(promo: PromoCode): string {
    return promo.maxUses === null ? `${promo.usedCount} / ∞` : `${promo.usedCount} / ${promo.maxUses}`;
  }

  formatDateRange(promo: PromoCode): string {
    return `${this.fmtDate(promo.validFrom)} – ${this.fmtDate(promo.validTo)}`;
  }

  private fmtDate(d: string): string {
    const date = new Date(d + 'T00:00:00');
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
}
