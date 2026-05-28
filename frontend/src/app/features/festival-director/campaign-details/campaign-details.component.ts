import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ad, CampaignWorkspace } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-campaign-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './campaign-details.component.html',
  styleUrls: ['./campaign-details.component.css']
})
export class CampaignDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  workspace: CampaignWorkspace | null = null;
  errorMessage = '';
  processingAdId: number | null = null;
  rejectingAd: Ad | null = null;
  rejectReason = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    this.campaignService.getDirectorCampaignWorkspace(festivalId).subscribe({
      next: workspace => this.workspace = workspace,
      error: () => this.errorMessage = 'Error loading campaign.'
    });
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

  get currentRole(): string {
    return this.currentUser?.assignment?.festivalRole ?? '';
  }

  canReviewAd(ad: Ad): boolean {
    return ad.currentPhaseAssignedRole === this.currentRole;
  }

  canManagePromotion(ad: Ad): boolean {
    return ad.status.toUpperCase() === 'PUBLISHED';
  }

  openPromotion(ad: Ad): void {
    if (!this.workspace) return;
    void this.router.navigate(
      ['/director/festivals', this.workspace.campaign.festivalId, 'campaign', 'ads', ad.adId],
      { queryParams: { [ad.promotion ? 'prolong' : 'publish']: '1' } }
    );
  }

  approve(ad: Ad): void {
    if (!this.workspace) return;
    this.processingAdId = ad.adId;
    this.campaignService.approveDirectorAd(this.workspace.campaign.festivalId, ad.adId).subscribe({
      next: () => {
        this.processingAdId = null;
        void this.router.navigate(
          ['/director/festivals', this.workspace?.campaign.festivalId, 'campaign', 'ads', ad.adId],
          { queryParams: { publish: '1' } }
        );
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err?.error?.message ?? 'Error approving ad.';
      }
    });
  }

  openRejectModal(ad: Ad): void {
    this.rejectingAd = ad;
    this.rejectReason = '';
    this.errorMessage = '';
  }

  closeRejectModal(): void {
    this.rejectingAd = null;
    this.rejectReason = '';
  }

  reject(): void {
    if (!this.workspace || !this.rejectingAd || !this.rejectReason.trim()) return;
    const ad = this.rejectingAd;
    this.processingAdId = ad.adId;
    this.campaignService.rejectDirectorAd(this.workspace.campaign.festivalId, ad.adId, this.rejectReason.trim()).subscribe({
      next: () => {
        this.processingAdId = null;
        this.closeRejectModal();
        this.ngOnInit();
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err?.error?.message ?? 'Error rejecting ad.';
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
