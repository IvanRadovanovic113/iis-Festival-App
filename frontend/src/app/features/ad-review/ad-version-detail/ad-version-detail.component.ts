import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { AdVersionDetail } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-version-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ad-version-detail.component.html',
  styleUrls: ['./ad-version-detail.component.css']
})
export class AdVersionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  version: AdVersionDetail | null = null;
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    const versionNumber = Number(this.route.snapshot.paramMap.get('versionNumber'));
    this.campaignService.getAdVersionDetail(festivalId, adId, versionNumber).subscribe({
      next: version => this.version = version,
      error: () => this.errorMessage = 'Error loading ad version.'
    });
  }

  get audience(): 'manager' | 'director' {
    return (this.route.snapshot.data['audience'] as 'manager' | 'director') ?? 'manager';
  }

  get backLink(): string[] {
    const festivalId = this.route.snapshot.paramMap.get('festivalId') ?? '';
    const adId = this.route.snapshot.paramMap.get('adId') ?? '';
    return this.audience === 'director'
      ? ['/director/festivals', festivalId, 'campaign', 'ads', adId]
      : ['/manager/festivals', festivalId, 'campaign', 'ads', adId];
  }

  isImageContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:image/');
  }

  isAudioContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:audio/');
  }

  isVideoContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:video/');
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

  logout(): void {
    this.authService.logout();
  }
}
