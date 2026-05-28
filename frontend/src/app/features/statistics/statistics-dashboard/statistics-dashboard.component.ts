import { Component, OnInit, inject, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import {
  StatisticsPhaseCount,
  StatisticsResponse,
  StatisticsTypeCount
} from '../../../core/models/campaign.model';

@Component({
  selector: 'app-statistics-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './statistics-dashboard.component.html',
  styleUrls: ['./statistics-dashboard.component.css']
})
export class StatisticsDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);
  private readonly route = inject(ActivatedRoute);
  @ViewChild('pdfContent') pdfContent?: ElementRef<HTMLElement>;

  readonly currentUser = this.authService.getCurrentUser();
  statistics: StatisticsResponse | null = null;
  errorMessage = '';
  loading = false;
  downloading = false;

  selectedCampaignId: number | null = null;
  selectedAdTypeId: number | null = null;
  dateFrom = '';
  dateTo = '';

  private readonly phasePalette = ['#5fc24c', '#8ad56a', '#55c0d8', '#3fa88f', '#9be65d', '#2f7d32', '#6fdad0'];
  private readonly typePalette = ['#6b3d57', '#874f6f', '#b86f80', '#d88ea1', '#93617f', '#cf9eb0'];
  private readonly statusChartHeight = 240;

  ngOnInit(): void {
    this.loadStatistics();
  }

  get audience(): 'manager' | 'director' {
    return (this.route.snapshot.data['audience'] as 'manager' | 'director') ?? 'manager';
  }

  get pageTitle(): string {
    return this.audience === 'director' ? 'Director statistics' : 'Manager statistics';
  }

  get festivalLink(): string {
    return this.audience === 'director' ? '/director/festivals' : '/manager/festivals';
  }

  get notificationsLink(): string {
    return this.audience === 'director' ? '/director/notifications' : '/manager/notifications';
  }

  get phaseCards(): StatisticsPhaseCount[] {
    return this.statistics?.phaseCounts ?? [];
  }

  get typeLegend(): Array<StatisticsTypeCount & { color: string; percentage: number; dash: string; offset: string }> {
    const total = this.statistics?.typeCounts.reduce((sum, item) => sum + item.count, 0) ?? 0;
    let cumulative = 0;
    return (this.statistics?.typeCounts ?? []).map((item, index) => {
      const fraction = total === 0 ? 0 : item.count / total;
      const length = fraction * 100;
      const offset = -cumulative;
      cumulative += length;
      return {
        ...item,
        color: this.typePalette[index % this.typePalette.length],
        percentage: total === 0 ? 0 : Math.round(fraction * 100),
        dash: `${length} ${100 - length}`,
        offset: `${offset}`
      };
    });
  }

  get statusBars(): Array<StatisticsPhaseCount & { color: string; height: number }> {
    const maxCount = Math.max(...(this.statistics?.phaseCounts.map(item => item.count) ?? [0]), 1);
    return (this.statistics?.phaseCounts ?? []).map((item, index) => ({
      ...item,
      color: this.phasePalette[index % this.phasePalette.length],
      height: Math.max(item.count === 0 ? 0 : 14, (item.count / maxCount) * this.statusChartHeight)
    }));
  }

  get statusChartMax(): number {
    return Math.max(...(this.statistics?.phaseCounts.map(item => item.count) ?? [0]), 1);
  }

  get statusAxisTicks(): number[] {
    const max = this.statusChartMax;
    const steps = 4;
    return Array.from({ length: steps + 1 }, (_, index) => Math.round((max / steps) * (steps - index)));
  }

  get displayName(): string {
    return this.currentUser?.username || 'User';
  }

  get avatarLabel(): string {
    const name = this.displayName.trim();
    const parts = name.split(/[._-]+/).filter(Boolean);
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }

  loadStatistics(): void {
    this.loading = true;
    this.errorMessage = '';
    this.campaignService.getStatistics({
      campaignId: this.selectedCampaignId,
      dateFrom: this.dateFrom || null,
      dateTo: this.dateTo || null,
      adTypeId: this.selectedAdTypeId
    }).subscribe({
      next: statistics => {
        this.statistics = statistics;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error loading statistics.';
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.selectedCampaignId = null;
    this.selectedAdTypeId = null;
    this.dateFrom = '';
    this.dateTo = '';
    this.loadStatistics();
  }

  onFilterChanged(): void {
    this.loadStatistics();
  }

  async downloadPdf(): Promise<void> {
    if (!this.statistics || !this.pdfContent?.nativeElement || this.downloading) return;

    this.downloading = true;
    try {
      const canvas = await html2canvas(this.pdfContent.nativeElement, {
        backgroundColor: '#f8f6ef',
        scale: 2,
        useCORS: true
      });

      const imageData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const margin = 10;
      const contentWidth = pageWidth - margin * 2;
      const contentHeight = (canvas.height * contentWidth) / canvas.width;

      let remainingHeight = contentHeight;
      let positionY = margin;

      pdf.addImage(imageData, 'PNG', margin, positionY, contentWidth, contentHeight);
      remainingHeight -= pageHeight - margin * 2;

      while (remainingHeight > 0) {
        pdf.addPage();
        positionY = margin - (contentHeight - remainingHeight);
        pdf.addImage(imageData, 'PNG', margin, positionY, contentWidth, contentHeight);
        remainingHeight -= pageHeight - margin * 2;
      }

      pdf.save('festival-statistics.pdf');
    } finally {
      this.downloading = false;
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
