import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { AuthService } from '../../../core/services/auth.service';
import { AnalyticsService } from '../../../core/services/analytics.service';
import {
  SalesOverview, RevenueDataPoint, SalesByTicketType, TicketTypeSummary,
  TicketTypeSalesDataPoint, TicketTypePriceHistoryItem, TicketTypeSalesByPeriodItem,
  PromoCodeStats, ActionsAnalytics, DynamicPricingAnalytics
} from '../../../core/models/analytics.model';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule],
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css']
})
export class AnalyticsDashboardComponent implements OnInit, OnDestroy {
  @ViewChild('lineCanvas')         lineCanvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('barCanvas')          barCanvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('salesDetailCanvas')  salesDetailCanvasRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('priceHistoryCanvas') priceHistoryCanvasRef?: ElementRef<HTMLCanvasElement>;

  private authService      = inject(AuthService);
  private analyticsService = inject(AnalyticsService);
  private destroyRef       = inject(DestroyRef);

  private lineChart: Chart | null         = null;
  private barChart: Chart | null          = null;
  private salesDetailChart: Chart | null  = null;
  private priceHistoryChart: Chart | null = null;

  private festivalId: number | null = null;
  pdfLoading = false;

  // Overview state
  loading = true;
  error = '';
  overview: SalesOverview | null = null;
  private pendingRevenue: RevenueDataPoint[] = [];
  private pendingByType: SalesByTicketType[] = [];

  // Per-type state
  selectedTicketTypeId: number | null = null;
  detailLoading = false;
  detailError = '';
  salesOverTime: TicketTypeSalesDataPoint[] = [];
  priceHistory: TicketTypePriceHistoryItem[] = [];
  salesByPeriod: TicketTypeSalesByPeriodItem[] = [];

  fromDate: string = this.daysAgo(30);
  toDate: string   = this.today();

  // Promo & bundle deal state
  promoCodes: PromoCodeStats[] = [];
  actionsData: ActionsAnalytics | null = null;

  // Dynamic pricing state
  dynamicPricingData: DynamicPricingAnalytics | null = null;

  ngOnInit(): void {
    const festivalId = this.authService.getCurrentUser()?.assignment?.festivalId;
    if (!festivalId) {
      this.error = 'No festival assignment found.';
      this.loading = false;
      return;
    }
    this.festivalId = festivalId;

    forkJoin({
      overview:       this.analyticsService.getOverview(festivalId),
      revenue:        this.analyticsService.getRevenueOverTime(festivalId),
      byType:         this.analyticsService.getSalesByTicketType(festivalId),
      promoCodes:     this.analyticsService.getPromoCodeStats(festivalId),
      actions:        this.analyticsService.getActionsAnalytics(festivalId),
      dynamicPricing: this.analyticsService.getDynamicPricingAnalytics(festivalId)
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ overview, revenue, byType, promoCodes, actions, dynamicPricing }) => {
          this.overview            = overview;
          this.pendingRevenue      = revenue;
          this.pendingByType       = byType;
          this.promoCodes          = promoCodes;
          this.actionsData         = actions;
          this.dynamicPricingData  = dynamicPricing;
          this.loading             = false;
          setTimeout(() => this.renderOverviewCharts(), 0);
        },
        error: err => {
          this.error   = err.error?.message || 'Failed to load analytics.';
          this.loading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.lineChart?.destroy();
    this.barChart?.destroy();
    this.salesDetailChart?.destroy();
    this.priceHistoryChart?.destroy();
  }

  soldPercent(tt: TicketTypeSummary): number {
    if (!tt.totalQuantity) return 0;
    return Math.round((tt.soldCount / tt.totalQuantity) * 100);
  }

  onTicketTypeChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    if (!val) {
      this.selectedTicketTypeId = null;
      this.salesDetailChart?.destroy();
      this.priceHistoryChart?.destroy();
      this.salesDetailChart  = null;
      this.priceHistoryChart = null;
      return;
    }
    this.selectTicketType(+val);
  }

  selectTicketType(id: number): void {
    this.selectedTicketTypeId = id;
    this.detailLoading = true;
    this.detailError   = '';
    this.salesDetailChart?.destroy();
    this.priceHistoryChart?.destroy();
    this.salesDetailChart  = null;
    this.priceHistoryChart = null;

    forkJoin({
      sales:   this.analyticsService.getSalesOverTime(id, this.fromDate, this.toDate),
      price:   this.analyticsService.getPriceHistory(id),
      periods: this.analyticsService.getSalesByPeriod(id)
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ sales, price, periods }) => {
          this.salesOverTime = sales;
          this.priceHistory  = price;
          this.salesByPeriod = periods;
          this.detailLoading = false;
          setTimeout(() => this.renderDetailCharts(), 0);
        },
        error: err => {
          this.detailError   = err.error?.message || 'Failed to load ticket type data.';
          this.detailLoading = false;
        }
      });
  }

  applyFilter(): void {
    if (!this.selectedTicketTypeId) return;

    this.analyticsService
      .getSalesOverTime(this.selectedTicketTypeId, this.fromDate, this.toDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.salesOverTime = data;
          this.buildSalesDetailChart(data);
        },
        error: err => {
          this.detailError = err.error?.message || 'Failed to update chart.';
        }
      });
  }

  downloadPdf(): void {
    if (!this.festivalId) return;
    this.pdfLoading = true;
    this.analyticsService.generatePdfReport(this.festivalId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `izvestaj-${new Date().toISOString().split('T')[0]}.pdf`;
          a.click();
          URL.revokeObjectURL(url);
          this.pdfLoading = false;
        },
        error: () => {
          this.pdfLoading = false;
        }
      });
  }

  // ── Chart builders ────────────────────────────────────────────────────────

  private renderOverviewCharts(): void {
    this.buildLineChart(this.pendingRevenue);
    this.buildBarChart(this.pendingByType);
  }

  private renderDetailCharts(): void {
    this.buildSalesDetailChart(this.salesOverTime);
    this.buildPriceHistoryChart(this.priceHistory);
  }

  private buildLineChart(data: RevenueDataPoint[]): void {
    if (!this.lineCanvasRef) return;
    this.lineChart?.destroy();
    this.lineChart = new Chart(this.lineCanvasRef.nativeElement, {
      type: 'line',
      data: {
        labels: data.map(d => d.date),
        datasets: [{
          data: data.map(d => d.revenue),
          label: 'Revenue',
          fill: true,
          tension: 0.3,
          borderColor: '#4f46e5',
          backgroundColor: 'rgba(79, 70, 229, 0.08)',
          pointRadius: 4,
          pointBackgroundColor: '#4f46e5'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { callback: v => '€' + v } } }
      }
    });
  }

  private buildBarChart(data: SalesByTicketType[]): void {
    if (!this.barCanvasRef) return;
    const palette = ['#4f46e5', '#7c3aed', '#db2777', '#dc2626', '#d97706', '#059669'];
    this.barChart?.destroy();
    this.barChart = new Chart(this.barCanvasRef.nativeElement, {
      type: 'bar',
      data: {
        labels: data.map(d => d.name),
        datasets: [{
          data: data.map(d => d.revenue),
          label: 'Revenue',
          backgroundColor: data.map((_, i) => palette[i % palette.length])
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { callback: v => '€' + v } } }
      }
    });
  }

  private buildSalesDetailChart(data: TicketTypeSalesDataPoint[]): void {
    if (!this.salesDetailCanvasRef) return;
    this.salesDetailChart?.destroy();
    this.salesDetailChart = new Chart(this.salesDetailCanvasRef.nativeElement, {
      type: 'line',
      data: {
        labels: data.map(d => d.date),
        datasets: [{
          data: data.map(d => d.ticketsSold),
          label: 'Tickets Sold',
          fill: true,
          tension: 0.3,
          borderColor: '#059669',
          backgroundColor: 'rgba(5, 150, 105, 0.08)',
          pointRadius: 4,
          pointBackgroundColor: '#059669'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
      }
    });
  }

  private buildPriceHistoryChart(data: TicketTypePriceHistoryItem[]): void {
    if (!this.priceHistoryCanvasRef) return;
    this.priceHistoryChart?.destroy();
    this.priceHistoryChart = new Chart(this.priceHistoryCanvasRef.nativeElement, {
      type: 'line',
      data: {
        labels: data.map(d => d.datum.substring(0, 10)),
        datasets: [{
          data: data.map(d => d.newPrice),
          label: 'Price (€)',
          stepped: true,
          borderColor: '#d97706',
          backgroundColor: 'rgba(217, 119, 6, 0.08)',
          pointRadius: 5,
          pointBackgroundColor: data.map(d => d.manual ? '#dc2626' : '#d97706')
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              afterLabel: (ctx) => {
                const item = data[ctx.dataIndex];
                return item.manual ? `Manual: ${item.reason}` : `Auto: ${item.reason}`;
              }
            }
          }
        },
        scales: { y: { beginAtZero: false, ticks: { callback: v => '€' + v } } }
      }
    });
  }

  // ── Utilities ─────────────────────────────────────────────────────────────

  private today(): string {
    return new Date().toISOString().split('T')[0];
  }

  private daysAgo(n: number): string {
    const d = new Date();
    d.setDate(d.getDate() - n);
    return d.toISOString().split('T')[0];
  }
}
