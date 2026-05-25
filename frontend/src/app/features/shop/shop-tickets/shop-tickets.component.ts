import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ShopService } from '../../../core/services/shop.service';
import { ShopTicketType } from '../../../core/models/shop.model';

interface FestivalGroup {
  festivalId: number;
  festivalName: string;
  tickets: ShopTicketType[];
}

@Component({
  selector: 'app-shop-tickets',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './shop-tickets.component.html',
  styleUrls: ['./shop-tickets.component.css']
})
export class ShopTicketsComponent implements OnInit {
  private shopService = inject(ShopService);
  private destroyRef = inject(DestroyRef);

  groups: FestivalGroup[] = [];
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.shopService.getAvailableTicketTypes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: tickets => {
          this.groups = this.groupByFestival(tickets);
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Failed to load tickets. Please try again.';
          this.loading = false;
        }
      });
  }

  private groupByFestival(tickets: ShopTicketType[]): FestivalGroup[] {
    const map = new Map<number, FestivalGroup>();
    for (const t of tickets) {
      if (!map.has(t.festivalId)) {
        map.set(t.festivalId, { festivalId: t.festivalId, festivalName: t.festivalName, tickets: [] });
      }
      map.get(t.festivalId)!.tickets.push(t);
    }
    return Array.from(map.values());
  }

  bundleLabel(b: { kupiKarata: number; dobijaKarata: number }): string {
    return `Buy ${b.kupiKarata} Get ${b.dobijaKarata} Free`;
  }

  availabilityPct(t: ShopTicketType): number {
    return Math.round((t.available / t.totalQuantity) * 100);
  }
}
