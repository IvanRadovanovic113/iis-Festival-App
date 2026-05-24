import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignWorkspace } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-campaign-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './campaign-details.component.html',
  styleUrls: ['./campaign-details.component.css']
})
export class CampaignDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  workspace: CampaignWorkspace | null = null;
  errorMessage = '';

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    this.campaignService.getDirectorCampaignWorkspace(festivalId).subscribe({
      next: workspace => this.workspace = workspace,
      error: () => this.errorMessage = 'Error loading campaign.'
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
