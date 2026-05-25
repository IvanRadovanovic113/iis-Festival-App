import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ShopService } from '../../../core/services/shop.service';
import { PurchaseResult } from '../../../core/models/shop.model';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './my-tickets.component.html',
  styleUrls: ['./my-tickets.component.css']
})
export class MyTicketsComponent implements OnInit {
  private shopService = inject(ShopService);
  private destroyRef = inject(DestroyRef);

  purchases: PurchaseResult[] = [];
  loading = true;
  errorMessage = '';

  /** Set of kupovinaId-ova čija je lista karata otvorena */
  expanded = new Set<number>();

  ngOnInit(): void {
    this.shopService.getMyPurchases()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.purchases = data;
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Could not load your purchases. Please try again.';
          this.loading = false;
        }
      });
  }

  toggle(id: number): void {
    if (this.expanded.has(id)) {
      this.expanded.delete(id);
    } else {
      this.expanded.add(id);
    }
  }

  isExpanded(id: number): boolean {
    return this.expanded.has(id);
  }

  statusClass(status: string): string {
    return 'status-' + status.toLowerCase();
  }
}
