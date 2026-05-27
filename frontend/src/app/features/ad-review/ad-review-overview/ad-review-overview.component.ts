import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { AdPhase, AdReview } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-review-overview',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ad-review-overview.component.html',
  styleUrls: ['./ad-review-overview.component.css']
})
export class AdReviewOverviewComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  review: AdReview | null = null;
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    this.campaignService.getAdReview(festivalId, adId).subscribe({
      next: review => this.review = review,
      error: () => this.errorMessage = 'Error loading ad history.'
    });
  }

  get audience(): 'manager' | 'director' {
    return (this.route.snapshot.data['audience'] as 'manager' | 'director') ?? 'manager';
  }

  get backLink(): string[] {
    const festivalId = this.route.snapshot.paramMap.get('festivalId') ?? '';
    return this.audience === 'director'
      ? ['/director/festivals', festivalId, 'campaign']
      : ['/manager/festivals', festivalId, 'campaign'];
  }

  get versionBaseLink(): string[] {
    const festivalId = this.route.snapshot.paramMap.get('festivalId') ?? '';
    const adId = this.route.snapshot.paramMap.get('adId') ?? '';
    return this.audience === 'director'
      ? ['/director/festivals', festivalId, 'campaign', 'ads', adId, 'versions']
      : ['/manager/festivals', festivalId, 'campaign', 'ads', adId, 'versions'];
  }

  versionLink(versionNumber: number): string[] {
    return [...this.versionBaseLink, String(versionNumber)];
  }

  private get currentPhaseOrderIndex(): number {
    return this.review?.flow.find(phase => phase.phaseId === this.review?.ad.currentPhaseId)?.orderIndex ?? 0;
  }

  isCompleted(phase: AdPhase): boolean {
    return this.currentPhaseOrderIndex > phase.orderIndex;
  }

  isCurrent(phase: AdPhase): boolean {
    return this.review?.ad.currentPhaseId === phase.phaseId;
  }

  logout(): void {
    this.authService.logout();
  }
}
