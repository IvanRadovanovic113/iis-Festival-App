import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { FestivalCampaignOverview } from '../../../core/models/campaign.model';
import { FESTIVAL_STATUS_LABELS, FestivalStatus } from '../../../core/models/festival.model';

@Component({
  selector: 'app-manager-festival-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './manager-festival-list.component.html',
  styleUrls: ['./manager-festival-list.component.css']
})
export class ManagerFestivalListComponent implements OnInit {
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  festivals: FestivalCampaignOverview[] = [];
  errorMessage = '';
  currentUser = this.authService.getCurrentUser();
  statusLabels = FESTIVAL_STATUS_LABELS;
  campaignsCount = 0;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getManagerFestivalOverviews().subscribe({
      next: festivals => {
        this.festivals = festivals;
        this.campaignsCount = festivals.filter(festival => festival.hasCampaign).length;
      },
      error: () => this.errorMessage = 'Error loading festivals.'
    });
  }

  getStatusLabel(status: string): string {
    return this.statusLabels[status as FestivalStatus] ?? status;
  }

  logout(): void {
    this.authService.logout();
  }
}
