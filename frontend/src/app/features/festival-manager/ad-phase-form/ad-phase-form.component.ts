import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { AdPhase, AdType } from '../../../core/models/campaign.model';

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
  draftTypeName = '';
  draftSelectedPhaseIds: number[] = [];
  readonly currentUser = this.authService.getCurrentUser();
  readonly assignableRoles = [
    { value: 'PRODUCT_DESIGNER', label: 'Product Designer' },
    { value: 'TECHNICAL_SUPPORT', label: 'Technical Support' }
  ];

  form = this.fb.group({
    name: ['', Validators.required],
    adTypeId: [null as number | null, Validators.required],
    description: ['', Validators.required],
    orderIndex: [1, [Validators.required, Validators.min(1)]],
    emailNotification: [true, Validators.required],
    assignedRole: ['PRODUCT_DESIGNER', Validators.required]
  });

  ngOnInit(): void {
    this.draftTypeName = this.route.snapshot.queryParamMap.get('draftTypeName') ?? '';
    this.restoreDraftFlow();
    this.campaignService.getAdTypes().subscribe({
      next: adTypes => {
        this.adTypes = adTypes;
        const adTypeId = this.route.snapshot.queryParamMap.get('adTypeId');
        if (adTypeId) {
          this.form.patchValue({ adTypeId: Number(adTypeId) });
        }
        const draftOrderIndex = Number(this.route.snapshot.queryParamMap.get('draftOrderIndex'));
        if (!Number.isNaN(draftOrderIndex) && draftOrderIndex > 0) {
          this.form.patchValue({ orderIndex: draftOrderIndex });
        }
        if (this.draftTypeName) {
          this.form.get('adTypeId')!.clearValidators();
          this.form.get('adTypeId')!.updateValueAndValidity();
        }
      },
      error: () => this.errorMessage = 'Error loading ad types.'
    });
  }

  private restoreDraftFlow(): void {
    const selectedPhaseIdsParam = this.route.snapshot.queryParamMap.get('selectedPhaseIds') ?? '';
    this.draftSelectedPhaseIds = selectedPhaseIdsParam
      ? selectedPhaseIdsParam.split(',').map(value => Number(value)).filter(value => !Number.isNaN(value))
      : [];
  }

  get selectedAdType(): AdType | undefined {
    const selectedId = this.form.value.adTypeId;
    return this.adTypes.find(adType => adType.adTypeId === selectedId);
  }

  get phaseFlowItems(): Array<{ phaseId: number | null; name: string; description: string; assignedRole: string; isDraft: boolean }> {
    const basePhases = this.getBaseFlowPhases();
    const insertIndex = this.getDraftInsertIndex(basePhases.length);
    const items = [...basePhases];
    items.splice(insertIndex, 0, {
      phaseId: null,
      name: this.form.value.name?.trim() || 'New phase',
      description: this.form.value.description?.trim() || 'Phase description will appear here.',
      assignedRole: this.getAssignedRoleLabel(this.form.value.assignedRole ?? 'PRODUCT_DESIGNER'),
      isDraft: true
    });
    return items;
  }

  private getBaseFlowPhases(): Array<{ phaseId: number | null; name: string; description: string; assignedRole: string; isDraft: boolean }> {
    if (this.draftTypeName) {
      return this.draftSelectedPhaseIds
        .map(phaseId => this.findPhaseById(phaseId))
        .filter((phase): phase is AdPhase => !!phase)
        .map(phase => ({
          phaseId: phase.phaseId,
          name: phase.name,
          description: phase.description,
          assignedRole: this.getAssignedRoleLabel(phase.assignedRole),
          isDraft: false
        }));
    }

    return (this.selectedAdType?.phases ?? []).map(phase => ({
      phaseId: phase.phaseId,
      name: phase.name,
      description: phase.description,
      assignedRole: this.getAssignedRoleLabel(phase.assignedRole),
      isDraft: false
    }));
  }

  private findPhaseById(phaseId: number): AdPhase | undefined {
    for (const adType of this.adTypes) {
      const match = adType.phases.find(phase => phase.phaseId === phaseId);
      if (match) return match;
    }
    return undefined;
  }

  private getDraftInsertIndex(flowLength: number): number {
    const requested = Number(this.form.value.orderIndex ?? 1);
    return Math.max(0, Math.min(requested - 1, flowLength));
  }

  moveFlowItem(currentIndex: number, direction: -1 | 1): void {
    const items = [...this.phaseFlowItems];
    const nextIndex = currentIndex + direction;
    if (nextIndex < 0 || nextIndex >= items.length) return;
    [items[currentIndex], items[nextIndex]] = [items[nextIndex], items[currentIndex]];

    const draftIndex = items.findIndex(item => item.isDraft);
    const reorderedPhaseIds = items.filter(item => !item.isDraft).map(item => item.phaseId!) as number[];
    this.draftSelectedPhaseIds = reorderedPhaseIds;
    this.form.patchValue({ orderIndex: draftIndex + 1 });
  }

  private buildReturnPhaseIds(newPhaseId: number): number[] {
    const insertIndex = this.getDraftInsertIndex(this.draftSelectedPhaseIds.length);
    return [
      ...this.draftSelectedPhaseIds.slice(0, insertIndex),
      newPhaseId,
      ...this.draftSelectedPhaseIds.slice(insertIndex)
    ];
  }

  getAssignedRoleLabel(role: string): string {
    return this.assignableRoles.find(item => item.value === role)?.label ?? role;
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
      adTypeId: number | null;
      description: string;
      orderIndex: number;
      emailNotification: boolean;
      assignedRole: string;
    }).subscribe({
      next: (response: any) => {
        const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
        this.router.navigate(['/manager/campaigns', campaignId, 'ad-types', 'new'], {
          queryParams: {
            phaseId: response.phaseId,
            draftTypeName: this.route.snapshot.queryParamMap.get('draftTypeName') ?? '',
            draftTypeDescription: this.route.snapshot.queryParamMap.get('draftTypeDescription') ?? '',
            draftTypeContentType: this.route.snapshot.queryParamMap.get('draftTypeContentType') ?? '',
            selectedPhaseIds: this.buildReturnPhaseIds(response.phaseId).join(','),
            draftOrderIndex: this.form.value.orderIndex ?? 1
          }
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
