import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { CreativeCampaign } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-creative-ad-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './creative-ad-list.component.html',
  styleUrls: ['./creative-ad-list.component.css']
})
export class CreativeAdListComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  campaigns: CreativeCampaign[] = [];
  filteredCampaigns: CreativeCampaign[] = [];
  selectedFestival = '';
  sortBy: 'startDateAsc' | 'startDateDesc' | 'eligibleAdsDesc' | 'eligibleAdsAsc' = 'startDateAsc';
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    this.load();
  }

  get roleLabel(): string {
    const role = this.currentUser?.assignment?.festivalRole;
    return role === 'PRODUCT_DESIGNER' ? 'Product designer' : 'Technical support';
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

  get festivalOptions(): string[] {
    return Array.from(new Set(this.campaigns.map(campaign => campaign.festivalName)));
  }

  get totalEligibleAds(): number {
    return this.campaigns.reduce((sum, campaign) => sum + campaign.eligibleAds, 0);
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getCreativeCampaigns().subscribe({
      next: campaigns => {
        this.campaigns = campaigns;
        this.applyFilters();
      },
      error: () => this.errorMessage = 'Error loading campaigns.'
    });
  }

  applyFilters(): void {
    this.filteredCampaigns = this.campaigns
      .filter(campaign => {
      const matchesFestival = !this.selectedFestival || campaign.festivalName === this.selectedFestival;
      return matchesFestival;
      })
      .sort((left, right) => {
        switch (this.sortBy) {
          case 'startDateDesc':
            return right.startDate.localeCompare(left.startDate);
          case 'eligibleAdsDesc':
            return right.eligibleAds - left.eligibleAds;
          case 'eligibleAdsAsc':
            return left.eligibleAds - right.eligibleAds;
          case 'startDateAsc':
          default:
            return left.startDate.localeCompare(right.startDate);
        }
      });
  }

  resetFilters(): void {
    this.selectedFestival = '';
    this.sortBy = 'startDateAsc';
    this.applyFilters();
  }

  logout(): void {
    this.authService.logout();
  }
}
