import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms'; // DODATO za pretragu u modalu
import { OfferService } from '../../../../core/services/offer.service';
import { PerformerService } from '../../../../core/services/performer.service';
import { OfferDetailResponse, OfferStatus } from '../../../../core/models/offer.model';
import { PerformerResponse } from '../../../../core/models/performer.model';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';

@Component({
  selector: 'app-offer-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './offer-detail.component.html',
  styleUrls: ['./offer-detail.component.css']
})
export class OfferDetailComponent implements OnInit {
  offer!: OfferDetailResponse;
  templateName: string = 'Loading...';
  isLoading = true;

  // Modal stanja
  showModal = false;
  modalPerformers: PerformerResponse[] = [];
  searchTerm = '';
  modalPage = 0;
  modalTotalPages = 0;

  constructor(
    private route: ActivatedRoute,
    private offerService: OfferService,
    private performerService: PerformerService,
    private workflowService: WorkflowTemplateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadOfferDetails();
  }

  loadOfferDetails(): void {
    const id = Number(this.route.snapshot.paramMap.get('offerId'));
    this.offerService.getOfferById(id).subscribe({
      next: (data) => {
        this.offer = data;
        this.isLoading = false;
        this.loadTemplateName(data.workflowTemplateId);
      }
    });
  }

  loadTemplateName(templateId: number): void {
    this.workflowService.getTemplateById(templateId).subscribe({
      next: (template) => {
        this.templateName = template.name;
      },
      error: () => this.templateName = 'Unknown Template'
    });
  }

  openAddPerformerModal(): void {
    this.showModal = true;
    this.searchTerm = '';
    this.modalPage = 0;
    this.searchPerformers();
  }

  closeModal(): void {
    this.showModal = false;
  }

  searchPerformers(): void {
    const filters = { searchName: this.searchTerm };
    this.performerService.getPerformers(filters, this.modalPage, 5).subscribe({
      next: (pageRes) => {
        this.modalPerformers = pageRes.content;
        this.modalTotalPages = pageRes.totalPages;
      }
    });
  }

  removePerformer(performerId: number): void {
    this.offerService.removeInterestedPerformer(this.offer.offerId, performerId).subscribe({
      next: () => {
        this.loadOfferDetails();
      },
      error: (err) => console.error('Error removing performer', err)
    });
  }

  onSearchChange(): void {
    this.modalPage = 0;
    this.searchPerformers();
  }

  prevModalPage(): void {
    if (this.modalPage > 0) {
      this.modalPage--;
      this.searchPerformers();
    }
  }

  nextModalPage(): void {
    if (this.modalPage < this.modalTotalPages - 1) {
      this.modalPage++;
      this.searchPerformers();
    }
  }

  selectPerformer(performerId: number): void {
    this.offerService.addInterestedPerformer(this.offer.offerId, performerId).subscribe({
      next: () => {
        this.closeModal();
        this.loadOfferDetails();
      },
      error: (err) => console.error('Error adding performer', err)
    });
  }

  getInitials(name: string): string {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  }

  publish(): void {
    this.offerService.publishOffer(this.offer.offerId).subscribe({
      next: () => this.loadOfferDetails()
    });
  }

  archive(): void {
    if (confirm('Are you sure you want to archive this offer?')) {
      this.offerService.archiveOffer(this.offer.offerId).subscribe({
        next: () => this.loadOfferDetails()
      });
    }
  }

  isDraft() { return this.offer?.status === OfferStatus.DRAFT; }
  isPublished() { return this.offer?.status === OfferStatus.PUBLISHED; }
  isArchived() { return this.offer?.status === OfferStatus.ARCHIVED; }
}