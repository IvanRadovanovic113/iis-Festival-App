import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { FestivalCampaignOverview } from '../../../core/models/campaign.model';
import { FESTIVAL_STATUS_LABELS, FestivalStatus } from '../../../core/models/festival.model';

@Component({
  selector: 'app-director-festival-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './director-festival-list.component.html',
  styleUrls: ['./director-festival-list.component.css']
})
export class DirectorFestivalListComponent implements OnInit {
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  festivals: FestivalCampaignOverview[] = [];
  errorMessage = '';
  statusLabels = FESTIVAL_STATUS_LABELS;
  currentUser = this.authService.getCurrentUser();
  campaignCount = 0;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getDirectorFestivalOverviews().subscribe({
      next: festivals => {
        this.festivals = festivals;
        this.campaignCount = festivals.filter(festival => festival.hasCampaign).length;
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
