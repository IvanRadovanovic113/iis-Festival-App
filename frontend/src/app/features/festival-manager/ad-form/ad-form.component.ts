import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { AdType, CampaignWorkspace } from '../../../core/models/campaign.model';

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
  errorMessage = '';
  saving = false;
  selectedFileName = '';

  form = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    adTypeId: [null as number | null, Validators.required],
    contentFileName: ['', Validators.required]
  });

  ngOnInit(): void {
    const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
    const festivalId = Number(this.route.snapshot.queryParamMap.get('festivalId'));
    if (festivalId) {
      this.loadWorkspace(festivalId);
    }
    this.campaignService.getAdTypes().subscribe({
      next: adTypes => {
        this.adTypes = adTypes;
        const selectedAdTypeId = this.route.snapshot.queryParamMap.get('adTypeId');
        if (selectedAdTypeId) {
          this.form.patchValue({ adTypeId: Number(selectedAdTypeId) });
        }
      },
      error: () => this.errorMessage = 'Error loading ad types.'
    });
    if (!festivalId && campaignId) {
      this.resolveWorkspaceFromFestivalList(campaignId);
    }
  }

  private loadWorkspace(festivalId: number): void {
    this.campaignService.getManagerCampaignWorkspace(festivalId).subscribe({
      next: workspace => this.workspace = workspace,
      error: () => this.errorMessage = 'Error loading campaign.'
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.selectedFileName = file?.name ?? '';
    this.form.patchValue({ contentFileName: this.selectedFileName });
  }

  save(): void {
    if (!this.workspace || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.campaignService.createAd(this.workspace.campaign.campaignId, this.form.getRawValue() as {
      name: string;
      description: string;
      adTypeId: number;
      contentFileName: string;
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
