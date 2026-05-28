import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OfferService } from '../../../../core/services/offer.service';
import { OfferResponse, OfferStatus } from '../../../../core/models/offer.model';

@Component({
  selector: 'app-offer-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './offer-list.component.html',
  styleUrls: ['./offer-list.component.css']
})
export class OfferListComponent implements OnInit {
  offers: OfferResponse[] = [];
  totalOffers = 0;
  currentPage = 0;
  pageSize = 10;
  selectedStatus: string = '';
  
  statuses = Object.values(OfferStatus);
  searchTerm: string = ''; // Sada se koristi za serversko filtriranje

  constructor(private offerService: OfferService) {}

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    const statusParam = this.selectedStatus ? (this.selectedStatus as OfferStatus) : undefined;
    
    // Izmenjeno: Prosleđujemo i 'searchTerm' u servis
    this.offerService.getOffers(statusParam, this.currentPage, this.pageSize, this.searchTerm).subscribe({
      next: (response) => {
        this.offers = response.content;
        this.totalOffers = response.totalElements;
      },
      error: (err) => console.error('Error loading offers:', err)
    });
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadOffers();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadOffers();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadOffers();
  }
}