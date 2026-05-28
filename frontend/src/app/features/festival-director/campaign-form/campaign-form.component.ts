import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignManagerOption, FestivalCampaignOverview } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-campaign-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './campaign-form.component.html',
  styleUrls: ['./campaign-form.component.css']
})
export class CampaignFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  festival: FestivalCampaignOverview | null = null;
  managers: CampaignManagerOption[] = [];
  errorMessage = '';
  saving = false;
  campaignExists = false;

  form = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    managerUserId: [null as number | null, Validators.required]
  });

  ngOnInit(): void {
    const festivalId = Number(this.route.snapshot.paramMap.get('festivalId'));
    this.campaignService.getDirectorFestivalOverviews().subscribe({
      next: festivals => {
        this.festival = festivals.find(item => item.festivalId === festivalId) ?? null;
        if (!this.festival) {
          this.errorMessage = 'Festival was not found.';
          return;
        }
        this.campaignExists = this.festival.hasCampaign;
        if (this.festival.hasCampaign && !this.isEditMode) {
          this.router.navigate(['/director/festivals', festivalId, 'campaign']);
          return;
        }
        this.loadManagers(festivalId);
      },
      error: () => this.errorMessage = 'Error loading festival.'
    });
  }

  private loadManagers(festivalId: number): void {
    this.campaignService.getFestivalManagers(festivalId).subscribe({
      next: managers => {
        this.managers = managers;
        if (this.isEditMode) {
          this.loadExistingCampaign(festivalId);
        }
      },
      error: () => this.errorMessage = 'Error loading festival managers.'
    });
  }

  get isEditMode(): boolean {
    return this.route.snapshot.routeConfig?.path?.endsWith('/edit') ?? false;
  }

  get title(): string {
    return this.isEditMode ? 'Edit campaign' : 'Create campaign';
  }

  get submitLabel(): string {
    if (this.saving) {
      return this.isEditMode ? 'Saving...' : 'Creating...';
    }
    return this.isEditMode ? 'Save changes' : 'Create campaign';
  }

  private loadExistingCampaign(festivalId: number): void {
    this.campaignService.getDirectorCampaign(festivalId).subscribe({
      next: campaign => {
        this.form.patchValue({
          name: campaign.name,
          description: campaign.description,
          startDate: campaign.startDate,
          endDate: campaign.endDate,
          managerUserId: campaign.managerUserId
        });
      },
      error: () => this.errorMessage = 'Error loading campaign.'
    });
  }

  save(): void {
    if (!this.festival || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const request = this.form.getRawValue() as {
      name: string;
      description: string;
      startDate: string;
      endDate: string;
      managerUserId: number;
    };
    const action = this.isEditMode
      ? this.campaignService.updateCampaign(this.festival.festivalId, request)
      : this.campaignService.createCampaign(this.festival.festivalId, request);

    action.subscribe({
      next: () => this.router.navigate(['/director/festivals', this.festival!.festivalId, 'campaign']),
      error: err => {
        this.errorMessage = err?.error?.message ?? `Error ${this.isEditMode ? 'updating' : 'creating'} campaign.`;
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
