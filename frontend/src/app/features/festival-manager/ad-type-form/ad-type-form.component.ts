import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { AdPhase } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-ad-type-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ad-type-form.component.html',
  styleUrls: ['./ad-type-form.component.css']
})
export class AdTypeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  phases: AdPhase[] = [];
  errorMessage = '';
  saving = false;
  readonly contentTypes = ['Video', 'Text', 'Audio', 'Image', 'Interactive'];

  form = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    contentType: ['', Validators.required],
    phaseIds: [[] as number[]]
  });

  ngOnInit(): void {
    this.campaignService.getAdPhases().subscribe({
      next: phases => {
        this.phases = phases;
        const highlightedPhase = this.route.snapshot.queryParamMap.get('phaseId');
        if (highlightedPhase) {
          this.togglePhase(Number(highlightedPhase), true);
        }
      },
      error: () => this.errorMessage = 'Error loading phases.'
    });
  }

  get selectedPhaseIds(): number[] {
    return this.form.value.phaseIds ?? [];
  }

  togglePhase(phaseId: number, forceSelected?: boolean): void {
    const current = new Set(this.selectedPhaseIds);
    if (forceSelected === true || !current.has(phaseId)) {
      current.add(phaseId);
    } else {
      current.delete(phaseId);
    }
    this.form.patchValue({ phaseIds: Array.from(current) });
  }

  save(): void {
    if (this.form.invalid || this.selectedPhaseIds.length === 0) {
      this.form.markAllAsTouched();
      this.errorMessage = this.selectedPhaseIds.length === 0 ? 'Select at least one phase.' : '';
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.campaignService.createAdType(this.form.getRawValue() as {
      name: string;
      description: string;
      contentType: string;
      phaseIds: number[];
    }).subscribe({
      next: (response: any) => {
        const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
        this.router.navigate(['/manager/campaigns', campaignId, 'ads', 'new'], {
          queryParams: { adTypeId: response.adTypeId }
        });
      },
      error: err => {
        this.errorMessage = err?.error?.message ?? 'Error creating ad type.';
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
