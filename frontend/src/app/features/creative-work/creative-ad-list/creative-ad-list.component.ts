import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { Ad } from '../../../core/models/campaign.model';

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

  ads: Ad[] = [];
  filteredAds: Ad[] = [];
  search = '';
  selectedType = '';
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

  get typeOptions(): string[] {
    return Array.from(new Set(this.ads.map(ad => ad.typeName)));
  }

  get festivalOptions(): string[] {
    return Array.from(new Set(this.ads.map(ad => ad.festivalName)));
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getCreativeAds().subscribe({
      next: ads => {
        this.ads = ads;
        this.applyFilters();
      },
      error: () => this.errorMessage = 'Error loading assigned ads.'
    });
  }

  applyFilters(): void {
    const term = this.search.trim().toLowerCase();
    this.filteredAds = this.ads.filter(ad => {
      const matchesSearch = !term
        || ad.name.toLowerCase().includes(term)
        || ad.campaignName.toLowerCase().includes(term);
      const matchesType = !this.selectedType || ad.typeName === this.selectedType;
      const matchesFestival = !this.selectedFestival || ad.festivalName === this.selectedFestival;
      return matchesSearch && matchesType && matchesFestival;
    });
  }

  resetFilters(): void {
    this.search = '';
    this.selectedType = '';
    this.selectedFestival = '';
    this.applyFilters();
  }

  logout(): void {
    this.authService.logout();
  }
}
