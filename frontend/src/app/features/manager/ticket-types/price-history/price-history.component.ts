import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CenovnaIstorijaService } from '../../../../core/services/cenovna-istorija.service';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { CenovnaIstorija } from '../../../../core/models/cenovna-istorija.model';
import { TicketType } from '../../../../core/models/ticket-type.model';

@Component({
  selector: 'app-price-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './price-history.component.html',
  styleUrls: ['./price-history.component.css']
})
export class PriceHistoryComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private historyService = inject(CenovnaIstorijaService);
  private ticketTypeService = inject(TicketTypeService);

  ticketTypeId!: number;
  ticketType: TicketType | null = null;
  history: CenovnaIstorija[] = [];
  errorMessage = '';
  loading = false;

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 20;

  ngOnInit(): void {
    this.ticketTypeId = +this.route.snapshot.paramMap.get('id')!;
    this.ticketTypeService.getById(this.ticketTypeId).subscribe({
      next: tt => this.ticketType = tt,
      error: () => {}
    });
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading = true;
    this.historyService.getHistory(this.ticketTypeId, page, this.pageSize).subscribe({
      next: data => {
        this.history = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error loading price history.';
        this.loading = false;
      }
    });
  }

  prevPage(): void {
    if (this.currentPage > 0) this.loadPage(this.currentPage - 1);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) this.loadPage(this.currentPage + 1);
  }

  priceDirection(entry: CenovnaIstorija): 'up' | 'down' | 'same' {
    if (entry.novaCena > entry.staraCena) return 'up';
    if (entry.novaCena < entry.staraCena) return 'down';
    return 'same';
  }
}
