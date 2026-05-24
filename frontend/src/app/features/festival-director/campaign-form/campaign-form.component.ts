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
        if (this.festival.hasCampaign) {
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
      next: managers => this.managers = managers,
      error: () => this.errorMessage = 'Error loading festival managers.'
    });
  }

  save(): void {
    if (!this.festival || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.campaignService.createCampaign(this.festival.festivalId, this.form.getRawValue() as {
      name: string;
      description: string;
      startDate: string;
      endDate: string;
      managerUserId: number;
    }).subscribe({
      next: () => this.router.navigate(['/director/festivals', this.festival!.festivalId, 'campaign']),
      error: err => {
        this.errorMessage = err?.error?.message ?? 'Error creating campaign.';
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
