import { Component, OnDestroy, OnInit, inject } from '@angular/core';
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
export class AdVersionDetailComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  version: AdVersionDetail | null = null;
  errorMessage = '';
  mediaPreviewUrl = '';
  private protectedPreviewObjectUrl: string | null = null;
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    const versionNumber = Number(this.route.snapshot.paramMap.get('versionNumber'));
    this.campaignService.getAdVersionDetail(festivalId, adId, versionNumber).subscribe({
      next: version => {
        this.revokeProtectedPreviewUrl();
        this.version = version;
        this.loadProtectedPreview(version.contentUrl, version.contentValue);
      },
      error: () => this.errorMessage = 'Error loading ad version.'
    });
  }

  ngOnDestroy(): void {
    this.revokeProtectedPreviewUrl();
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

  isImageContent(): boolean {
    return this.version?.contentMimeType?.startsWith('image/') ?? !!this.version?.contentValue?.startsWith('data:image/');
  }

  isAudioContent(): boolean {
    return this.version?.contentMimeType?.startsWith('audio/') ?? !!this.version?.contentValue?.startsWith('data:audio/');
  }

  isVideoContent(): boolean {
    return this.version?.contentMimeType?.startsWith('video/') ?? !!this.version?.contentValue?.startsWith('data:video/');
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

  private loadProtectedPreview(contentUrl: string | null, contentValue: string | null): void {
    this.revokeProtectedPreviewUrl();
    if (contentUrl) {
      this.campaignService.getProtectedMediaUrl(contentUrl).subscribe({
        next: objectUrl => {
          this.protectedPreviewObjectUrl = objectUrl;
          this.mediaPreviewUrl = objectUrl;
        },
        error: () => {
          this.mediaPreviewUrl = '';
          this.errorMessage = 'Error loading content preview.';
        }
      });
      return;
    }

    this.mediaPreviewUrl = contentValue?.startsWith('data:') ? contentValue : '';
  }

  private revokeProtectedPreviewUrl(): void {
    if (this.protectedPreviewObjectUrl) {
      URL.revokeObjectURL(this.protectedPreviewObjectUrl);
      this.protectedPreviewObjectUrl = null;
    }
  }
}
