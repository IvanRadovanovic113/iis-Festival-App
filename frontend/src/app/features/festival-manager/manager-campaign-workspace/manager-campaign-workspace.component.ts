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

  logout(): void {
    this.authService.logout();
  }
}
