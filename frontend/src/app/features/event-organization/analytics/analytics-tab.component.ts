import { Component, Input, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { Stage } from '../../../core/models/bina.model';
import { ResourceAnalytics, ResourceStageOccupancy, ResourceTopResource } from '../../../core/models/event-organization.model';
import { ResourceAnalyticsService } from '../../../core/services/resource-analytics.service';

type Period = 'MONTH' | 'YEAR' | 'ALL_TIME';

@Component({
  selector: 'app-event-organization-analytics-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics-tab.component.html',
  styleUrls: ['./analytics-tab.component.css']
})
export class AnalyticsTabComponent implements OnInit {
  @Input() stages: Stage[] = [];
  @ViewChild('pdfContent') pdfContent?: ElementRef<HTMLElement>;

  private readonly analyticsService = inject(ResourceAnalyticsService);

  private readonly resourceColors = ['#5b8eed', '#cf4d7b', '#5bb8a0', '#f4a259', '#7d5ba6'];

  analytics: ResourceAnalytics | null = null;
  loading = false;
  errorMessage = '';
  downloading = false;

  period: Period = 'MONTH';
  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;
  selectedStageId: number | null = null;

  readonly monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  readonly availableYears = this.buildYearOptions();

  ngOnInit(): void {
    this.loadAnalytics();
  }

  get selectedStageName(): string | null {
    return this.stages.find(s => s.stageId === this.selectedStageId)?.name ?? null;
  }

  get periodLabel(): string {
    if (this.period === 'MONTH') return `${this.monthNames[this.selectedMonth - 1]} ${this.selectedYear}`;
    if (this.period === 'YEAR') return String(this.selectedYear);
    return 'All time';
  }

  get stageOccupancyBars(): Array<ResourceStageOccupancy & { barHeight: string; color: string }> {
    const colors = ['#cf4d7b', '#5bb8a0', '#f4a259', '#7d5ba6', '#5b8eed'];
    return (this.analytics?.stageOccupancies ?? []).map((stage, index) => ({
      ...stage,
      barHeight: `${Math.max(stage.occupancyPercent, stage.totalReservations > 0 ? 4 : 0)}%`,
      color: colors[index % colors.length]
    }));
  }

  get topResources(): ResourceTopResource[] {
    return this.analytics?.topResources ?? [];
  }

  get topResourcesWithColors(): Array<ResourceTopResource & { color: string }> {
    return this.topResources.map((resource, index) => ({
      ...resource,
      color: this.resourceColors[index % this.resourceColors.length]
    }));
  }

  get maxTopResourceCount(): number {
    return Math.max(...this.topResources.map(r => r.requestCount), 1);
  }

  loadAnalytics(): void {
    this.loading = true;
    this.errorMessage = '';

    const year = this.period !== 'ALL_TIME' ? this.selectedYear : null;
    const month = this.period === 'MONTH' ? this.selectedMonth : null;
    const stageId = this.selectedStageId;

    this.analyticsService.getAnalytics(year, month, stageId).subscribe({
      next: data => {
        this.analytics = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load analytics data.';
        this.loading = false;
      }
    });
  }

  setPeriod(period: Period): void {
    this.period = period;
    this.loadAnalytics();
  }

  onFilterChanged(): void {
    this.loadAnalytics();
  }

  async downloadPdf(): Promise<void> {
    if (!this.analytics || !this.pdfContent?.nativeElement || this.downloading) return;

    this.downloading = true;
    try {
      const canvas = await html2canvas(this.pdfContent.nativeElement, {
        backgroundColor: '#ffffff',
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
      pdf.addImage(imageData, 'PNG', margin, margin, contentWidth, contentHeight);
      remainingHeight -= pageHeight - margin * 2;

      while (remainingHeight > 0) {
        pdf.addPage();
        const positionY = margin - (contentHeight - remainingHeight);
        pdf.addImage(imageData, 'PNG', margin, positionY, contentWidth, contentHeight);
        remainingHeight -= pageHeight - margin * 2;
      }

      pdf.save('resource-analytics.pdf');
    } finally {
      this.downloading = false;
    }
  }

  private buildYearOptions(): number[] {
    const currentYear = new Date().getFullYear();
    return [currentYear - 2, currentYear - 1, currentYear, currentYear + 1];
  }
}
