import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { BinaService } from '../../core/services/bina.service';
import { EventOrganizationService } from '../../core/services/event-organization.service';
import { Bina } from '../../core/models/bina.model';
import { EventResource, StageResource, TimetableSlot } from '../../core/models/event-organization.model';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-event-organization',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './event-organization.component.html',
  styleUrls: ['./event-organization.component.css']
})
export class EventOrganizationComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly stageService = inject(BinaService);
  private readonly eventOrganizationService = inject(EventOrganizationService);
  private readonly fb = inject(FormBuilder);

  currentUser: User | null = null;
  activeTab: 'resources' | 'timetable' = 'resources';
  errorMessage = '';
  successMessage = '';

  stages: Bina[] = [];
  resources: EventResource[] = [];
  stageResources: StageResource[] = [];
  timetableSlots: TimetableSlot[] = [];

  selectedStageId: number | null = null;
  editingResource: EventResource | null = null;
  editingStageResource: StageResource | null = null;

  resourceForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    type: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    totalQuantity: [1, [Validators.required, Validators.min(1)]]
  });

  stageResourceForm = this.fb.group({
    resourceId: [null as number | null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]]
  });

  timetableForm = this.fb.group({
    stageId: [null as number | null, Validators.required],
    date: [this.today(), Validators.required]
  });

  get selectedStage(): Bina | null {
    return this.stages.find(stage => stage.binaId === this.selectedStageId) ?? null;
  }

  get assignableResources(): EventResource[] {
    const assignedIds = new Set(this.stageResources.map(item => item.resourceId));
    return this.resources.filter(resource => !assignedIds.has(resource.id));
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.clearMessages();
    this.stageService.getAll().subscribe({
      next: stages => {
        this.stages = stages;
        this.selectedStageId = stages[0]?.binaId ?? null;
        this.timetableForm.patchValue({ stageId: this.selectedStageId });
        if (this.selectedStageId) {
          this.loadStageResources(this.selectedStageId);
          this.loadTimetable();
        }
      },
      error: () => this.errorMessage = 'Unable to load stages.'
    });
    this.loadResources();
  }

  loadResources(): void {
    this.eventOrganizationService.getResources().subscribe({
      next: resources => this.resources = resources,
      error: () => this.errorMessage = 'Unable to load resources.'
    });
  }

  selectStage(stageIdValue: string): void {
    this.selectedStageId = Number(stageIdValue);
    this.cancelStageResourceEdit();
    this.stageResourceForm.reset({ resourceId: null, quantity: 1 });
    this.loadStageResources(this.selectedStageId);
  }

  loadStageResources(stageId: number): void {
    this.eventOrganizationService.getStageResources(stageId).subscribe({
      next: resources => this.stageResources = resources,
      error: () => this.errorMessage = 'Unable to load stage resources.'
    });
  }

  saveResource(): void {
    if (this.resourceForm.invalid) {
      this.resourceForm.markAllAsTouched();
      return;
    }
    const value = this.resourceForm.getRawValue();
    const request = {
      name: value.name!,
      type: value.type!,
      description: value.description || null,
      totalQuantity: Number(value.totalQuantity)
    };
    const operation = this.editingResource
      ? this.eventOrganizationService.updateResource(this.editingResource.id, request)
      : this.eventOrganizationService.createResource(request);

    operation.subscribe({
      next: () => {
        this.successMessage = this.editingResource ? 'Resource updated.' : 'Resource created.';
        this.cancelResourceEdit();
        this.loadResources();
        if (this.selectedStageId) this.loadStageResources(this.selectedStageId);
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to save resource.'
    });
  }

  editResource(resource: EventResource): void {
    this.editingResource = resource;
    this.resourceForm.patchValue({
      name: resource.name,
      type: resource.type,
      description: resource.description ?? '',
      totalQuantity: resource.totalQuantity
    });
  }

  cancelResourceEdit(): void {
    this.editingResource = null;
    this.resourceForm.reset({ name: '', type: '', description: '', totalQuantity: 1 });
  }

  deleteResource(resource: EventResource): void {
    if (!confirm(`Delete resource "${resource.name}"?`)) return;
    this.eventOrganizationService.deleteResource(resource.id).subscribe({
      next: () => {
        this.successMessage = 'Resource deleted.';
        this.loadResources();
        if (this.selectedStageId) this.loadStageResources(this.selectedStageId);
      },
      error: () => this.errorMessage = 'Unable to delete resource.'
    });
  }

  saveStageResource(): void {
    if (!this.selectedStageId || this.stageResourceForm.invalid) {
      this.stageResourceForm.markAllAsTouched();
      return;
    }
    const value = this.stageResourceForm.getRawValue();
    const request = {
      resourceId: this.editingStageResource?.resourceId ?? Number(value.resourceId),
      quantity: Number(value.quantity)
    };
    const operation = this.editingStageResource
      ? this.eventOrganizationService.updateStageResource(this.selectedStageId, this.editingStageResource.resourceId, request)
      : this.eventOrganizationService.assignResourceToStage(this.selectedStageId, request);

    operation.subscribe({
      next: () => {
        this.successMessage = this.editingStageResource ? 'Stage resource updated.' : 'Resource assigned to stage.';
        this.cancelStageResourceEdit();
        this.loadStageResources(this.selectedStageId!);
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to save stage resource.'
    });
  }

  editStageResource(stageResource: StageResource): void {
    this.editingStageResource = stageResource;
    this.stageResourceForm.patchValue({
      resourceId: stageResource.resourceId,
      quantity: stageResource.quantity
    });
  }

  cancelStageResourceEdit(): void {
    this.editingStageResource = null;
    this.stageResourceForm.reset({ resourceId: null, quantity: 1 });
  }

  removeStageResource(stageResource: StageResource): void {
    if (!this.selectedStageId || !confirm(`Remove "${stageResource.resourceName}" from this stage?`)) return;
    this.eventOrganizationService.removeResourceFromStage(this.selectedStageId, stageResource.resourceId).subscribe({
      next: () => {
        this.successMessage = 'Resource removed from stage.';
        this.loadStageResources(this.selectedStageId!);
      },
      error: () => this.errorMessage = 'Unable to remove stage resource.'
    });
  }

  loadTimetable(): void {
    if (this.timetableForm.invalid) {
      this.timetableForm.markAllAsTouched();
      return;
    }
    const value = this.timetableForm.getRawValue();
    this.eventOrganizationService.getTimetable(Number(value.stageId), value.date!).subscribe({
      next: slots => this.timetableSlots = slots,
      error: () => this.errorMessage = 'Unable to load timetable.'
    });
  }

  logout(): void {
    this.authService.logout();
  }

  setTab(tab: 'resources' | 'timetable'): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
