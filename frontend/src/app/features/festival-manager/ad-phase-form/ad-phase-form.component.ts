import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { AdType } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-phase-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ad-phase-form.component.html',
  styleUrls: ['./ad-phase-form.component.css']
})
export class AdPhaseFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  adTypes: AdType[] = [];
  errorMessage = '';
  saving = false;

  form = this.fb.group({
    name: ['', Validators.required],
    adTypeId: [null as number | null, Validators.required],
    description: ['', Validators.required],
    orderIndex: [1, [Validators.required, Validators.min(1)]],
    emailNotification: [true, Validators.required]
  });

  ngOnInit(): void {
    this.campaignService.getAdTypes().subscribe({
      next: adTypes => {
        this.adTypes = adTypes;
        const adTypeId = this.route.snapshot.queryParamMap.get('adTypeId');
        if (adTypeId) {
          this.form.patchValue({ adTypeId: Number(adTypeId) });
        }
      },
      error: () => this.errorMessage = 'Error loading ad types.'
    });
  }

  get selectedAdType(): AdType | undefined {
    const selectedId = this.form.value.adTypeId;
    return this.adTypes.find(adType => adType.adTypeId === selectedId);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.campaignService.createAdPhase(this.form.getRawValue() as {
      name: string;
      adTypeId: number;
      description: string;
      orderIndex: number;
      emailNotification: boolean;
    }).subscribe({
      next: (response: any) => {
        const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
        this.router.navigate(['/manager/campaigns', campaignId, 'ad-types', 'new'], {
          queryParams: { phaseId: response.phaseId }
        });
      },
      error: err => {
        this.errorMessage = err?.error?.message ?? 'Error creating phase.';
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
