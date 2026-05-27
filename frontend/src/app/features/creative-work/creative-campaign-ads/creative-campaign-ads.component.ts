import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { Ad } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-creative-campaign-ads',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './creative-campaign-ads.component.html',
  styleUrls: ['./creative-campaign-ads.component.css']
})
export class CreativeCampaignAdsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  ads: Ad[] = [];
  filteredAds: Ad[] = [];
  campaignName = '';
  festivalName = '';
  festivalLocation = '';
  search = '';
  selectedType = '';
  selectedStatus = '';
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    this.load();
  }

  get roleLabel(): string {
    const role = this.currentUser?.assignment?.festivalRole;
    return role === 'PRODUCT_DESIGNER' ? 'Product designer' : 'Technical support';
  }

  get subtitle(): string {
    return this.festivalLocation ? `${this.festivalName} · ${this.festivalLocation}` : this.festivalName;
  }

  get typeOptions(): string[] {
    return Array.from(new Set(this.ads.map(ad => ad.typeName)));
  }

  get statusOptions(): string[] {
    return Array.from(new Set(this.ads.map(ad => ad.status)));
  }

  get campaignId(): number {
    return Number(this.route.snapshot.paramMap.get('campaignId'));
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getCreativeCampaignAds(this.campaignId).subscribe({
      next: ads => {
        this.ads = ads;
        this.filteredAds = ads;
        this.campaignName = ads[0]?.campaignName ?? 'Campaign';
        this.festivalName = ads[0]?.festivalName ?? this.currentUser?.assignment?.festivalName ?? '';
        this.festivalLocation = ads[0]?.festivalLocation ?? '';
        this.applyFilters();
      },
      error: () => this.errorMessage = 'Error loading campaign ads.'
    });
  }

  applyFilters(): void {
    const term = this.search.trim().toLowerCase();
    this.filteredAds = this.ads.filter(ad => {
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

  logout(): void {
    this.authService.logout();
  }
}
