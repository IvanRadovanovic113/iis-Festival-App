import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OfferService } from '../../../core/services/offer.service';
import { OfferStatus } from '../../../core/models/offer.model';
import { NegotiationService } from '../../../core/services/negotiation.service';
import { NegotiationResponse, PerformerStatsDto, StatePerformance, CriticalNegotiationDto } from '../../../core/models/negotiation.model';
import { forkJoin } from 'rxjs';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { jsPDF } from "jspdf";
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NgxChartsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  // Dinamički podaci sa bekenda (Offers)
  publishedOffersCount = 0;
  draftOffersCount = 0;
  archivedOffersCount = 0;
  totalOffersCount = 0;
  activeNegotiations: NegotiationResponse[] = [];
  performerStats: PerformerStatsDto[] = [];
  bottlenecks: StatePerformance[] = [];
  successRate = 'N/A';
  bottlenecksGrouped: { [key: string]: StatePerformance[] } = {};
  durationTrendData: any[] = [];
  efficiencyData: any = null;
  gaugeData: any[] = [];
  offerOutcomesData: any[] = [];
  criticalAlerts: CriticalNegotiationDto[] = [];

  constructor(private offerService: OfferService,
              private negotiationService: NegotiationService
  ) {}

  ngOnInit(): void {
    this.loadOfferStats();
    this.loadNegotiations();
    this.loadPerformerStats();
    this.loadBottlenecks();
    this.loadDurationTrend();
    this.loadEfficiency();
    this.loadOfferOutcomes();
    this.loadCriticalAlerts();
  }

  loadOfferStats(): void {
    forkJoin({
      draft: this.offerService.getOffers(OfferStatus.DRAFT, 0, 1),
      published: this.offerService.getOffers(OfferStatus.PUBLISHED, 0, 1),
      archived: this.offerService.getOffers(OfferStatus.ARCHIVED, 0, 1)
    }).subscribe({
      next: (res) => {
        this.draftOffersCount = res.draft.totalElements;
        this.publishedOffersCount = res.published.totalElements;
        this.archivedOffersCount = res.archived.totalElements;
        this.totalOffersCount = this.draftOffersCount + this.publishedOffersCount + this.archivedOffersCount;
      },
      error: (err) => console.error('Failed to load offer stats for dashboard', err)
    });
  }

loadNegotiations(): void {
    this.negotiationService.getAllNegotiations().subscribe({
      next: (data: any) => {
        const allNegotiations = data.content; 

        this.activeNegotiations = allNegotiations.filter((n: any) => n.status === 'ACTIVE');
        
        console.log('Broj aktivnih:', this.activeNegotiations.length);
      },
      error: (err) => console.error('Failed to load negotiations', err)
    });
  }

  loadPerformerStats(): void {
      this.negotiationService.getPerformerStats().subscribe(data => {
        this.performerStats = data;
        
        const totalSuccessful = data.reduce((acc, curr) => acc + curr.successfulNegotiations, 0);
        const totalFailed = data.reduce((acc, curr) => acc + curr.failedNegotiations, 0);
        const totalFinished = totalSuccessful + totalFailed;

        if (totalFinished > 0) {
          this.successRate = ((totalSuccessful / totalFinished) * 100).toFixed(0) + '%';
        } else {
          this.successRate = '0%';
        }
    });
  }

  loadBottlenecks(): void {
    this.negotiationService.getBottleneckReport().subscribe({
      next: (data) => {
        this.bottlenecksGrouped = data.reduce((acc, curr) => {
          if (!acc[curr.templateName]) {
            acc[curr.templateName] = [];
          }
          acc[curr.templateName].push(curr);
          return acc;
        }, {} as { [key: string]: StatePerformance[] });
      },
      error: (err) => console.error('Failed to load bottlenecks', err)
    });
  }

  get workflowNames(): string[] {
    return Object.keys(this.bottlenecksGrouped);
  }

  isBottleneck(state: StatePerformance, allStates: StatePerformance[]): boolean {
    const maxDuration = Math.max(...allStates.map(s => s.averageDurationHours));
    return state.averageDurationHours === maxDuration;
  }

  loadDurationTrend(): void {
    this.negotiationService.getNegotiationDurationTrend('2026-05-25', '2026-06-25', 'YYYY-MM-DD')
      .subscribe(data => {
        this.durationTrendData = [{
          name: 'Avg Duration (hours)',
          series: data.map(item => ({
            name: item.intervalLabel,
            value: item.avgValue
          }))
        }];
      });
  }

  loadEfficiency(): void {
    this.negotiationService.getNegotiationEfficiency('2026-05-25', '2026-06-25')
      .subscribe(data => {
        this.efficiencyData = data;
        this.gaugeData = [
          {
            name: 'Success Rate',
            value: data.successPercentage
          }
        ];
      });
  }

  loadOfferOutcomes(): void {
    this.negotiationService.getOfferOutcomes('2026-05-25', '2026-06-25')
      .subscribe(data => {
        this.offerOutcomesData = data.map(item => ({
          name: item.outcome.replace('_', ' '),
          value: item.count
        }));
      });
  }

  loadCriticalAlerts(): void {
    this.negotiationService.getCriticalAlerts().subscribe(data => {
      this.criticalAlerts = data;
    });
  }

  get activeNegotiationsCount(): number {
    return this.activeNegotiations ? this.activeNegotiations.length : 0;
  }

  downloadReport(): void {
    const doc = new jsPDF();
    let y = 20;

    // Naslov
    doc.setFontSize(18);
    doc.text('Izvestaj o pregovorima - Dashboard', 14, y);
    y += 10;
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(`Izvestaj kreiran: ${new Date().toLocaleString('sr-RS')}`, 14, y);
    doc.setTextColor(0);
    y += 20;

    // 1. Stat kartice
    autoTable(doc, {
      startY: y,
      head: [['Metrika', 'Vrednost']],
      body: [
        ['Aktivni pregovori', this.activeNegotiationsCount],
        ['Objavljene ponude', this.publishedOffersCount],
        ['Ukupna stopa uspeha', this.successRate]
      ],
    });
    y = (doc as any).lastAutoTable.finalY + 20;

    // 2. Top Performeri
    if (this.performerStats.length > 0) {
      doc.text('Top Performeri:', 14, y);
      autoTable(doc, {
        startY: y + 5,
        head: [['Ime', 'Uspeh', 'Stopa (%)']],
        body: this.performerStats.slice(0, 5).map(s => [
          s.stageName, 
          `${s.successfulNegotiations}/${s.totalNegotiations}`, 
          s.successRate.toFixed(0) + '%'
        ]),
      });
      y = (doc as any).lastAutoTable.finalY + 20;
    }

    // 3. Bottlenecks
    doc.text('Workflow Bottlenecks:', 14, y);
    y += 5;
    for (const workflow of this.workflowNames) {
      doc.setFontSize(11);
      doc.text(`Proces: ${workflow}`, 14, y);
      autoTable(doc, {
        startY: y + 2,
        head: [['Stanje', 'Trajanje (h)', 'Pregovori']],
        body: this.bottlenecksGrouped[workflow].map(b => [
          b.stateName, 
          b.averageDurationHours.toFixed(1) + 'h', 
          b.count
        ]),
      });
      y = (doc as any).lastAutoTable.finalY + 15; 
    }

    // 4. Duration Trend
    if (this.durationTrendData.length > 0) {
      doc.text('Trend trajanja pregovora:', 14, y);
      const trendRows = this.durationTrendData[0].series.map((item: any) => [item.name, item.value + 'h']);
      autoTable(doc, {
        startY: y + 5,
        head: [['Datum/Interval', 'Prosecno trajanje']],
        body: trendRows,
      });
      y = (doc as any).lastAutoTable.finalY + 20;
    }

    // 5. Ishodi ponuda
    doc.text('Distribucija ishoda ponuda:', 14, y);
    autoTable(doc, {
      startY: y + 5,
      head: [['Ishod', 'Broj']],
      body: this.offerOutcomesData.map(o => [o.name, o.value]),
    });
    y = (doc as any).lastAutoTable.finalY + 20;

    // 6. Efikasnost
    if (this.efficiencyData) {
      doc.text('Efikasnost pregovora:', 14, y);
      autoTable(doc, {
        startY: y + 5,
        head: [['Ukupno', 'Uspesno', 'Stopa uspesnosti']],
        body: [[
          this.efficiencyData.totalCount,
          this.efficiencyData.successfulCount,
          this.efficiencyData.successPercentage + '%'
        ]],
      });
    }

    doc.save('Izvestaj_Pregovori_sa_Izvodjacima.pdf');
  }
}