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
  search = '';
  selectedFestival = '';
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    this.load();
  }

  get roleLabel(): string {
    const role = this.currentUser?.assignment?.festivalRole;
    return role === 'PRODUCT_DESIGNER' ? 'Product designer' : 'Technical support';
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
    const term = this.search.trim().toLowerCase();
    this.filteredCampaigns = this.campaigns.filter(campaign => {
      const matchesSearch = !term
        || campaign.campaignName.toLowerCase().includes(term)
        || campaign.festivalName.toLowerCase().includes(term);
      const matchesFestival = !this.selectedFestival || campaign.festivalName === this.selectedFestival;
      return matchesSearch && matchesFestival;
    });
  }

  resetFilters(): void {
    this.search = '';
    this.selectedFestival = '';
    this.applyFilters();
  }

  logout(): void {
    this.authService.logout();
  }
}
