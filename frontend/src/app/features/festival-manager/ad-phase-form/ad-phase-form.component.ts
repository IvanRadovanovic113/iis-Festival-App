import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { AdPhase, AdType } from '../../../core/models/campaign.model';

type DraftFlowPhase = {
  phaseId: number;
  name: string;
  description: string;
  assignedRole: string;
};

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
  phases: AdPhase[] = [];
  errorMessage = '';
  saving = false;
  draftTypeName = '';
  draftSelectedPhaseIds: number[] = [];
  draftFlowPhases: DraftFlowPhase[] = [];
  phaseFlowItems: Array<{ phaseId: number | null; name: string; description: string; assignedRole: string; isDraft: boolean; locked: boolean }> = [];
  readonly currentUser = this.authService.getCurrentUser();
  readonly enforcedFinalPhases = [
    { name: 'DIRECTOR APPROVAL', assignedRole: 'Festival Director' },
    { name: 'PUBLISHED', assignedRole: 'Festival Manager' }
  ];
  readonly assignableRoles = [
    { value: 'FESTIVAL_MANAGER', label: 'Festival Manager' },
    { value: 'PRODUCT_DESIGNER', label: 'Product Designer' },
    { value: 'TECHNICAL_SUPPORT', label: 'Technical Support' },
    { value: 'FESTIVAL_DIRECTOR', label: 'Festival Director' }
  ];

  form = this.fb.group({
    name: ['', Validators.required],
    adTypeId: [null as number | null, Validators.required],
    description: ['', Validators.required],
    orderIndex: [1, [Validators.required, Validators.min(1)]],
    emailNotification: [true, Validators.required],
    assignedRole: ['FESTIVAL_MANAGER', Validators.required]
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

  ngOnInit(): void {
    this.draftTypeName = this.route.snapshot.queryParamMap.get('draftTypeName') ?? '';
    this.restoreDraftFlow();
    this.form.valueChanges.subscribe(() => this.refreshFlowPreview());
    forkJoin({
      adTypes: this.campaignService.getAdTypes(),
      phases: this.campaignService.getAdPhases()
    }).subscribe({
      next: ({ adTypes, phases }) => {
        this.adTypes = adTypes;
        this.phases = phases;
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
        this.refreshFlowPreview();
      },
      error: () => this.errorMessage = 'Error loading ad types.'
    });
    this.refreshFlowPreview();
  }

  private restoreDraftFlow(): void {
    const params = this.route.snapshot.queryParamMap;
    const selectedPhaseIdsParam = params.get('selectedPhaseIds') ?? '';
    this.draftSelectedPhaseIds = selectedPhaseIdsParam
      ? selectedPhaseIdsParam.split(',').map(value => Number(value)).filter(value => !Number.isNaN(value))
      : [];
    const draftFlowParam = params.get('draftFlow');
    if (!draftFlowParam) return;
    try {
      const parsed = JSON.parse(draftFlowParam) as DraftFlowPhase[];
      this.draftFlowPhases = Array.isArray(parsed) ? parsed.filter(phase => typeof phase?.name === 'string') : [];
    } catch {
      this.draftFlowPhases = [];
    }
  }

  get selectedAdType(): AdType | undefined {
    const selectedId = this.form.value.adTypeId;
    return this.adTypes.find(adType => adType.adTypeId === selectedId);
  }

  private buildPhaseFlowItems(): Array<{ phaseId: number | null; name: string; description: string; assignedRole: string; isDraft: boolean; locked: boolean }> {
    const editablePhases = this.getEditableFlowPhases();
    const insertIndex = this.getDraftInsertIndex(editablePhases.length);
    const items = [...editablePhases];
    items.splice(insertIndex, 0, {
      phaseId: null,
      name: this.form.value.name?.trim() || 'New phase',
      description: this.form.value.description?.trim() || 'Phase description will appear here.',
      assignedRole: this.getAssignedRoleLabel(this.form.value.assignedRole ?? 'FESTIVAL_MANAGER'),
      isDraft: true,
      locked: false
    });
    return [
      ...items,
      ...this.enforcedFinalPhases.map(phase => ({
        phaseId: null,
        name: phase.name,
        description: 'This phase is automatically appended to every ad type workflow.',
        assignedRole: phase.assignedRole,
        isDraft: false,
        locked: true
      }))
    ];
  }

  private refreshFlowPreview(): void {
    const items = this.buildPhaseFlowItems();
    this.phaseFlowItems = items.length > 0 ? items : [
      {
        phaseId: null,
        name: this.form.value.name?.trim() || 'New phase',
        description: this.form.value.description?.trim() || 'Phase description will appear here.',
        assignedRole: this.getAssignedRoleLabel(this.form.value.assignedRole ?? 'FESTIVAL_MANAGER'),
        isDraft: true,
        locked: false
      },
      ...this.enforcedFinalPhases.map(phase => ({
        phaseId: null,
        name: phase.name,
        description: 'This phase is automatically appended to every ad type workflow.',
        assignedRole: phase.assignedRole,
        isDraft: false,
        locked: true
      }))
    ];
  }

  private getEditableFlowPhases(): Array<{ phaseId: number | null; name: string; description: string; assignedRole: string; isDraft: boolean; locked: boolean }> {
    if (this.draftTypeName) {
      if (this.draftFlowPhases.length > 0) {
        return this.draftFlowPhases.map(phase => ({
          phaseId: phase.phaseId,
          name: phase.name,
          description: phase.description,
          assignedRole: this.getAssignedRoleLabel(phase.assignedRole),
          isDraft: false,
          locked: false
        }));
      }
      return this.draftSelectedPhaseIds
        .map(phaseId => this.findPhaseById(phaseId))
        .filter((phase): phase is AdPhase => !!phase)
        .map(phase => ({
          phaseId: phase.phaseId,
          name: phase.name,
          description: phase.description,
          assignedRole: this.getAssignedRoleLabel(phase.assignedRole),
          isDraft: false,
          locked: false
        }));
    }

    return (this.selectedAdType?.phases ?? [])
      .filter(phase => !this.isEnforcedTerminalPhase(phase.name))
      .map(phase => ({
        phaseId: phase.phaseId,
        name: phase.name,
        description: phase.description,
        assignedRole: this.getAssignedRoleLabel(phase.assignedRole),
        isDraft: false,
        locked: false
      }));
  }

  private findPhaseById(phaseId: number): AdPhase | undefined {
    return this.phases.find(phase => phase.phaseId === phaseId);
  }

  private isEnforcedTerminalPhase(name: string): boolean {
    const normalized = name.trim().toUpperCase();
    return normalized === 'DIRECTOR APPROVAL' || normalized === 'PUBLISHED';
  }

  private getDraftInsertIndex(editableFlowLength: number): number {
    const requested = Number(this.form.value.orderIndex ?? 1);
    return Math.max(0, Math.min(requested - 1, editableFlowLength));
  }

  moveFlowItem(currentIndex: number, direction: -1 | 1): void {
    const items = [...this.phaseFlowItems];
    const editableLimit = this.getEditableFlowPhases().length + 1;
    const nextIndex = currentIndex + direction;
    if (currentIndex >= editableLimit || nextIndex < 0 || nextIndex >= editableLimit) return;
    [items[currentIndex], items[nextIndex]] = [items[nextIndex], items[currentIndex]];

    const draftIndex = items.findIndex(item => item.isDraft);
    const reorderedPhaseIds = items
      .filter(item => !item.isDraft && !item.locked)
      .map(item => item.phaseId!) as number[];
    this.draftFlowPhases = items
      .filter(item => !item.isDraft && !item.locked)
      .map(item => ({
        phaseId: item.phaseId!,
        name: item.name,
        description: item.description,
        assignedRole: this.assignableRoles.find(role => role.label === item.assignedRole)?.value ?? item.assignedRole
      }));
    this.draftSelectedPhaseIds = reorderedPhaseIds;
    this.form.patchValue({ orderIndex: draftIndex + 1 });
    this.refreshFlowPreview();
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
            draftFlow: JSON.stringify(this.buildReturnFlow(response.phaseId)),
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

  private buildReturnFlow(newPhaseId: number): DraftFlowPhase[] {
    const insertIndex = this.getDraftInsertIndex(this.draftFlowPhases.length);
    const newPhase: DraftFlowPhase = {
      phaseId: newPhaseId,
      name: this.form.value.name?.trim() || 'New phase',
      description: this.form.value.description?.trim() || '',
      assignedRole: this.form.value.assignedRole ?? 'FESTIVAL_MANAGER'
    };

    return [
      ...this.draftFlowPhases.slice(0, insertIndex),
      newPhase,
      ...this.draftFlowPhases.slice(insertIndex)
    ];
  }
}
