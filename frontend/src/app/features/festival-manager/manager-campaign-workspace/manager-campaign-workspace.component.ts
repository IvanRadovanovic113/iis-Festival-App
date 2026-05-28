import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ad, CampaignWorkspace } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-manager-campaign-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './manager-campaign-workspace.component.html',
  styleUrls: ['./manager-campaign-workspace.component.css']
})
export class ManagerCampaignWorkspaceComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  workspace: CampaignWorkspace | null = null;
  filteredAds: Ad[] = [];
  errorMessage = '';
  search = '';
  selectedType = '';
  selectedStatus = '';
  processingAdId: number | null = null;
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    this.campaignService.getManagerCampaignWorkspace(festivalId).subscribe({
      next: workspace => {
        this.workspace = workspace;
        this.applyFilters();
      },
      error: () => this.errorMessage = 'Error loading campaign.'
    });
  }

  get typeOptions(): string[] {
    return Array.from(new Set((this.workspace?.ads ?? []).map(ad => ad.typeName)));
  }

  get statusOptions(): string[] {
    return Array.from(new Set((this.workspace?.ads ?? []).map(ad => ad.status)));
  }

  applyFilters(): void {
    const ads = this.workspace?.ads ?? [];
    const term = this.search.trim().toLowerCase();
    this.filteredAds = ads.filter(ad => {
      const matchesSearch = !term || ad.name.toLowerCase().includes(term);
      const matchesType = !this.selectedType || ad.typeName === this.selectedType;
      const matchesStatus = !this.selectedStatus || ad.status === this.selectedStatus;
      return matchesSearch && matchesType && matchesStatus;
    });
  }

  resetFilters(): void {
    this.search = '';
    this.selectedType = '';
    this.selectedStatus = '';
    this.applyFilters();
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

  canEditAd(ad: Ad): boolean {
    return ad.status.toUpperCase() !== 'PUBLISHED';
  }

  canDeleteAd(ad: Ad): boolean {
    return ad.status.toUpperCase() !== 'PUBLISHED';
  }

  approve(ad: Ad): void {
    if (!this.workspace) return;
    this.processingAdId = ad.adId;
    this.campaignService.approveManagerAd(this.workspace.campaign.festivalId, ad.adId).subscribe({
      next: () => {
        this.processingAdId = null;
        this.load();
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err?.error?.message ?? 'Error approving ad.';
      }
    });
  }

  reject(ad: Ad): void {
    if (!this.workspace) return;
    const reason = globalThis.prompt(`Why is "${ad.name}" rejected?`);
    if (!reason || !reason.trim()) return;
    this.processingAdId = ad.adId;
    this.campaignService.rejectManagerAd(this.workspace.campaign.festivalId, ad.adId, reason.trim()).subscribe({
      next: () => {
        this.processingAdId = null;
        this.load();
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err?.error?.message ?? 'Error rejecting ad.';
      }
    });
  }

  delete(ad: Ad): void {
    if (!this.workspace) return;
    const confirmed = globalThis.confirm(`Delete "${ad.name}"? This action cannot be undone.`);
    if (!confirmed) return;

    this.processingAdId = ad.adId;
    this.campaignService.deleteManagerAd(this.workspace.campaign.festivalId, ad.adId).subscribe({
      next: () => {
        this.processingAdId = null;
        this.load();
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err?.error?.message ?? 'Error deleting ad.';
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
