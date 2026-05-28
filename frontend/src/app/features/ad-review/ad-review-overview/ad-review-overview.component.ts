import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { AdPhase, AdReview } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-review-overview',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './ad-review-overview.component.html',
  styleUrls: ['./ad-review-overview.component.css']
})
export class AdReviewOverviewComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  review: AdReview | null = null;
  errorMessage = '';
  successMessage = '';
  readonly currentUser = this.authService.getCurrentUser();
  savingPromotion = false;
  showPromotionForm = false;
  selectedDuration: '2w' | '4w' | 'custom' = '2w';
  readonly promotionChannels = [
    { value: 'YOUTUBE', label: 'YouTube' },
    { value: 'BILLBOARD', label: 'Bilbord' },
    { value: 'INSTAGRAM', label: 'Instagram' },
    { value: 'TIKTOK', label: 'TikTok' },
    { value: 'RADIO', label: 'Radio' },
    { value: 'FACEBOOK', label: 'Facebook' }
  ];

  readonly promotionForm = this.fb.group({
    channel: ['YOUTUBE', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    pricePerDay: [1000, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    this.campaignService.getAdReview(festivalId, adId).subscribe({
      next: review => {
        this.review = review;
        this.patchPromotionForm();
        const shouldOpenPublish = this.route.snapshot.queryParamMap.get('publish') === '1';
        const shouldOpenProlong = this.route.snapshot.queryParamMap.get('prolong') === '1';
        this.showPromotionForm = this.canManagePromotion && (shouldOpenPublish || shouldOpenProlong || !review.ad.promotion);
        if (shouldOpenPublish && this.canManagePromotion) {
          this.successMessage = 'Ad approved. Complete the promotion details below.';
        } else if (shouldOpenProlong && this.canManagePromotion) {
          this.successMessage = 'Update the promotion details below to prolong this ad.';
        }
      },
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

  isImageContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:image/');
  }

  isAudioContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:audio/');
  }

  isVideoContent(contentValue: string | undefined): boolean {
    return !!contentValue && contentValue.startsWith('data:video/');
  }

  get canManagePromotion(): boolean {
    return this.audience === 'director' && this.review?.ad.status.toUpperCase() === 'PUBLISHED';
  }

  get promotionHeading(): string {
    return this.review?.ad.promotion ? 'Prolong promotion' : 'Publish ad';
  }

  get hasPromotion(): boolean {
    return !!this.review?.ad.promotion;
  }

  get promotionActionLabel(): string {
    return this.hasPromotion ? 'Prolong promotion' : 'Publish ad';
  }

  openPromotionForm(): void {
    this.showPromotionForm = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  selectDuration(duration: '2w' | '4w' | 'custom'): void {
    this.selectedDuration = duration;
    const startDate = this.promotionForm.value.startDate;
    if (!startDate || duration === 'custom') {
      return;
    }
    const days = duration === '2w' ? 14 : 28;
    this.promotionForm.patchValue({
      endDate: this.toDateInputValue(this.plusDays(new Date(startDate), days))
    });
  }

  savePromotion(): void {
    if (!this.review || this.promotionForm.invalid) {
      this.promotionForm.markAllAsTouched();
      return;
    }

    this.savingPromotion = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.campaignService.saveDirectorPromotion(
      Number(this.route.snapshot.paramMap.get('festivalId')),
      this.review.ad.adId,
      this.promotionForm.getRawValue() as { channel: string; startDate: string; endDate: string; pricePerDay: number; }
    ).subscribe({
      next: ad => {
        if (!this.review) return;
        this.review = { ...this.review, ad };
        this.patchPromotionForm();
        this.savingPromotion = false;
        this.showPromotionForm = false;
        this.successMessage = ad.promotion ? 'Promotion details saved.' : '';
      },
      error: err => {
        this.savingPromotion = false;
        this.errorMessage = err?.error?.message ?? 'Error saving promotion details.';
      }
    });
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

  private patchPromotionForm(): void {
    const promotion = this.review?.ad.promotion;
    const startDate = promotion?.startDate ?? this.toDateInputValue(new Date());
    const endDate = promotion?.endDate ?? this.toDateInputValue(this.plusDays(new Date(startDate), 14));
    this.promotionForm.patchValue({
      channel: promotion?.channel ?? 'YOUTUBE',
      startDate,
      endDate,
      pricePerDay: promotion?.pricePerDay ?? 1000
    });
    this.selectedDuration = this.resolveDuration(startDate, endDate);
  }

  private resolveDuration(startDate: string, endDate: string): '2w' | '4w' | 'custom' {
    const diffInDays = Math.round((new Date(endDate).getTime() - new Date(startDate).getTime()) / 86400000);
    if (diffInDays === 14) return '2w';
    if (diffInDays === 28) return '4w';
    return 'custom';
  }

  private plusDays(date: Date, days: number): Date {
    const next = new Date(date);
    next.setDate(next.getDate() + days);
    return next;
  }

  private toDateInputValue(date: Date): string {
    return date.toISOString().slice(0, 10);
  }
}
