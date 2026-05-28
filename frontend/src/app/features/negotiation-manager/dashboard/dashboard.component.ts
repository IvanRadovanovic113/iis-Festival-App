import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OfferService } from '../../../core/services/offer.service';
import { OfferStatus } from '../../../core/models/offer.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  // Dinamički podaci sa bekenda (Offers)
  publishedOffersCount = 0;
  draftOffersCount = 0;
  archivedOffersCount = 0;
  totalOffersCount = 0;

  // Hardkodovani podaci sa wireframe-a (Dok se ne implementiraju ostali servisi)
  activeNegotiationsCount = 7;
  contractsSignedCount = 12;
  successRate = '68%';

  activeNegotiations = [
    { performer: 'The Groove Band', offer: 'Main Stage · Jul 15', state: 'Contract Review', deadline: 'Overdue 2d', isOverdue: true },
    { performer: 'DJ Radovan', offer: 'Club Stage · Jul 16', state: 'Initial Contact', deadline: '4d left', isOverdue: false },
    { performer: 'Sara Mitrović', offer: 'Acoustic Stage · Jul 14', state: 'Terms Agreed', deadline: 'Overdue 1d', isOverdue: true },
    { performer: 'Neofolk Kolektiv', offer: 'Open Stage · Jul 17', state: 'Initial Contact', deadline: '6d left', isOverdue: false }
  ];

  recentActivities = [
    { type: 'negotiation', text: 'Negotiation with **The Groove Band** moved to Contract Review', time: '2h ago', dotClass: 'dot-blue' },
    { type: 'offer', text: 'Offer **Main Stage · Jul 15** frozen', time: '3h ago', dotClass: 'dot-gray' },
    { type: 'deadline', text: 'Deadline exceeded — Sara Mitrović - Terms Agreed', time: '1d ago', dotClass: 'dot-orange' },
    { type: 'contract', text: 'Contract signed — **Balkanski Ritam**', time: '2d ago', dotClass: 'dot-green' },
    { type: 'offer-pub', text: 'New offer published — **Club Stage · Jul 16**', time: '3d ago', dotClass: 'dot-green-light' }
  ];

  constructor(private offerService: OfferService) {}

  ngOnInit(): void {
    this.loadOfferStats();
  }

  loadOfferStats(): void {
    // forkJoin ispaljuje sve zahteve paralelno
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
}