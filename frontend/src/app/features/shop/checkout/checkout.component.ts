import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ShopService } from '../../../core/services/shop.service';
import { CheckoutPreview, KartaDto, PurchaseResult } from '../../../core/models/shop.model';

type View = 'checkout' | 'success';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule, RouterLink],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private shopService = inject(ShopService);
  private destroyRef = inject(DestroyRef);

  view: View = 'checkout';

  ticketTypeId = 0;
  quantity = 1;

  promoInput = '';
  appliedPromo: string | null = null;
  promoError = '';
  applyingPromo = false;

  preview: CheckoutPreview | null = null;
  previewLoading = true;
  previewError = '';

  purchasing = false;
  purchaseError = '';
  result: PurchaseResult | null = null;

  ngOnInit(): void {
    this.ticketTypeId = Number(this.route.snapshot.paramMap.get('ticketTypeId'));
    this.loadPreview();
  }

  // ─── Quantity ────────────────────────────────────────────────────────────

  decreaseQty(): void {
    if (this.quantity > 1) {
      this.quantity--;
      this.loadPreview();
    }
  }

  increaseQty(): void {
    const max = this.preview?.availableCount ?? 999;
    if (this.quantity >= max) return;
    this.quantity++;
    this.loadPreview();
  }

  // ─── Promo kod ───────────────────────────────────────────────────────────

  applyPromo(): void {
    const code = this.promoInput.trim().toUpperCase();
    if (!code) return;
    this.applyingPromo = true;
    this.promoError = '';
    this.shopService.preview(this.ticketTypeId, this.quantity, code)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.preview = data;
          this.appliedPromo = code;
          this.promoError = '';
          this.applyingPromo = false;
        },
        error: err => {
          this.promoError = err.error?.message || 'Invalid promo code.';
          this.applyingPromo = false;
        }
      });
  }

  removePromo(): void {
    this.appliedPromo = null;
    this.promoInput = '';
    this.promoError = '';
    this.loadPreview();
  }

  // ─── Preview ─────────────────────────────────────────────────────────────

  private loadPreview(): void {
    this.previewLoading = true;
    this.previewError = '';
    this.shopService.preview(this.ticketTypeId, this.quantity, this.appliedPromo)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.preview = data;
          this.previewLoading = false;
        },
        error: err => {
          this.previewError = err.error?.message || 'Could not load checkout info.';
          this.previewLoading = false;
        }
      });
  }

  // ─── Kupovina ────────────────────────────────────────────────────────────

  confirm(): void {
    if (this.purchasing || !this.preview) return;
    this.purchasing = true;
    this.purchaseError = '';
    this.shopService.purchase(this.ticketTypeId, this.quantity, this.appliedPromo)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.result = res;
          this.view = 'success';
          this.purchasing = false;
        },
        error: err => {
          this.purchaseError = err.error?.message || 'Purchase failed. Please try again.';
          this.purchasing = false;
        }
      });
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  discountRow(label: string, pct: number): string {
    return `${label} −${pct}%`;
  }

  tierLabel(name: string | null): string {
    if (!name) return '';
    const labels: Record<string, string> = { BRONZE: 'Bronze', SILVER: 'Silver', GOLD: 'Gold' };
    return labels[name] ?? name;
  }

  trackKarta(_: number, k: KartaDto): number { return k.kartaId; }
}
