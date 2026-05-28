import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ad, AdType, CampaignWorkspace } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ad-form.component.html',
  styleUrls: ['./ad-form.component.css']
})
export class AdFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  workspace: CampaignWorkspace | null = null;
  adTypes: AdType[] = [];
  ad: Ad | null = null;
  errorMessage = '';
  saving = false;
  readonly currentUser = this.authService.getCurrentUser();

  form = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    adTypeId: [null as number | null, Validators.required]
  });

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

  get isEditMode(): boolean {
    return this.route.snapshot.routeConfig?.path === 'manager/festivals/:festivalId/campaign/ads/:adId/edit';
  }

  ngOnInit(): void {
    const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
    const festivalId = Number(this.route.snapshot.queryParamMap.get('festivalId'));
    const editFestivalId = Number(this.route.snapshot.paramMap.get('festivalId'));

    if (this.isEditMode && editFestivalId) {
      this.loadWorkspace(editFestivalId);
      this.loadAd(editFestivalId);
    } else if (festivalId) {
      this.loadWorkspace(festivalId);
    }
    this.campaignService.getAdTypes().subscribe({
      next: adTypes => {
        this.adTypes = adTypes;
        if (this.ad) {
          this.form.patchValue({ adTypeId: this.ad.adTypeId });
        } else {
          const selectedAdTypeId = this.route.snapshot.queryParamMap.get('adTypeId');
          if (selectedAdTypeId) {
            this.form.patchValue({ adTypeId: Number(selectedAdTypeId) });
          }
        }
      },
      error: () => this.errorMessage = 'Error loading ad types.'
    });
    if (!this.isEditMode && !festivalId && campaignId) {
      this.resolveWorkspaceFromFestivalList(campaignId);
    }
  }

  private loadWorkspace(festivalId: number): void {
    this.campaignService.getManagerCampaignWorkspace(festivalId).subscribe({
      next: workspace => this.workspace = workspace,
      error: () => this.errorMessage = 'Error loading campaign.'
    });
  }

  private loadAd(festivalId: number): void {
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    this.campaignService.getManagerAd(festivalId, adId).subscribe({
      next: ad => {
        this.ad = ad;
        this.form.patchValue({
          name: ad.name,
          description: ad.description,
          adTypeId: ad.adTypeId
        });
      },
      error: () => this.errorMessage = 'Error loading ad.'
    });
  }

  private resolveWorkspaceFromFestivalList(campaignId: number): void {
    this.campaignService.getManagerFestivalOverviews().subscribe({
      next: festivals => {
        const match = festivals.find(festival => festival.campaignId === campaignId);
        if (match) {
          this.loadWorkspace(match.festivalId);
        }
      }
    });
  }

  get selectedAdType(): AdType | undefined {
    const selectedId = this.form.value.adTypeId ?? this.ad?.adTypeId;
    return this.adTypes.find(adType => adType.adTypeId === selectedId);
  }

  onAdTypeChanged(): void {
    this.errorMessage = '';
  }

  save(): void {
    if (!this.workspace || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    if (this.isEditMode && this.ad) {
      this.campaignService.updateManagerAd(this.workspace.campaign.festivalId, this.ad.adId, {
        name: this.form.value.name ?? '',
        description: this.form.value.description ?? ''
      }).subscribe({
        next: () => this.router.navigate(['/manager/festivals', this.workspace!.campaign.festivalId, 'campaign']),
        error: err => {
          this.errorMessage = err?.error?.message ?? 'Error updating ad.';
          this.saving = false;
        }
      });
      return;
    }

    this.campaignService.createAd(this.workspace.campaign.campaignId, this.form.getRawValue() as {
      name: string;
      description: string;
      adTypeId: number;
    }).subscribe({
      next: () => this.router.navigate(['/manager/festivals', this.workspace!.campaign.festivalId, 'campaign']),
      error: err => {
        this.errorMessage = err?.error?.message ?? 'Error creating ad.';
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
