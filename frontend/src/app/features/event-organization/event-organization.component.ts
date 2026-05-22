import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { BinaService } from '../../core/services/bina.service';
import { EventOrganizationService } from '../../core/services/event-organization.service';
import { Bina } from '../../core/models/bina.model';
import { EventResource, StageResource } from '../../core/models/event-organization.model';
import { User } from '../../core/models/user.model';

type MainTab = 'requests' | 'timetable' | 'resources' | 'tasks' | 'analytics';
type ResourceTab = 'manage' | 'inventory';
type ResourceModalMode = 'add' | 'edit';

interface InventoryRow {
  resource: EventResource;
  assignedQuantity: number;
  stageNames: string[];
  shared: boolean;
}

interface TimetableDay {
  key: string;
  dayName: string;
  dateLabel: string;
}

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
  activeTab: MainTab = 'requests';
  activeRequestFilter = 'All';
  activeResourceTab: ResourceTab = 'manage';
  errorMessage = '';
  successMessage = '';

  stages: Bina[] = [];
  resources: EventResource[] = [];
  stageResources: StageResource[] = [];
  allStageResources: StageResource[] = [];
  selectedStageId: number | null = null;

  modalMode: ResourceModalMode | null = null;
  editingStageResource: StageResource | null = null;
  deletingStageResource: StageResource | null = null;

  inventorySearch = '';
  inventoryStageFilter = 'All';
  timetableWeekOffset = 0;
  timetableHours = ['14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'];

  resourceForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    type: ['Equipment', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    stageId: [null as number | null, Validators.required],
    shareable: [false],
    note: ['']
  });

  get selectedStage(): Bina | null {
    return this.stages.find(stage => stage.binaId === this.selectedStageId) ?? null;
  }

  get inventoryRows(): InventoryRow[] {
    const rows = this.resources.map(resource => {
      const assignments = this.allStageResources.filter(item => item.resourceId === resource.id);
      const stageNames = assignments
        .map(item => this.stages.find(stage => stage.binaId === item.stageId)?.naziv)
        .filter((name): name is string => Boolean(name));

      return {
        resource,
        assignedQuantity: assignments.reduce((sum, item) => sum + item.quantity, 0),
        stageNames,
        shared: new Set(stageNames).size > 1
      };
    });

    return rows.filter(row => {
      const query = this.inventorySearch.trim().toLowerCase();
      const matchesSearch = !query
        || row.resource.name.toLowerCase().includes(query)
        || row.resource.type.toLowerCase().includes(query);
      const matchesStage = this.inventoryStageFilter === 'All' || row.stageNames.includes(this.inventoryStageFilter);
      return matchesSearch && matchesStage;
    });
  }

  get totalUnits(): number {
    return this.resources.reduce((sum, resource) => sum + resource.totalQuantity, 0);
  }

  get sharedResourceCount(): number {
    return this.inventoryRows.filter(row => row.shared).length;
  }

  get timetableDays(): TimetableDay[] {
    const start = this.weekStartDate();

    return ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((dayName, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);

      return {
        key: date.toISOString(),
        dayName,
        dateLabel: this.formatShortDate(date)
      };
    });
  }

  get timetableWeekLabel(): string {
    const start = this.weekStartDate();
    const end = new Date(start);
    end.setDate(start.getDate() + 6);

    return `${this.formatLongDate(start)} - ${this.formatLongDate(end)}`;
  }

  stageNameById(stageId: number): string {
    return this.stages.find(stage => stage.binaId === stageId)?.naziv ?? 'this stage';
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.clearMessages();
    forkJoin({
      stages: this.stageService.getAll(),
      resources: this.eventOrganizationService.getResources()
    }).subscribe({
      next: ({ stages, resources }) => {
        this.stages = stages;
        this.resources = resources;
        this.selectedStageId = stages[0]?.binaId ?? null;
        this.resourceForm.patchValue({ stageId: this.selectedStageId });
        this.refreshStageData();
      },
      error: () => this.errorMessage = 'Unable to load event organization data.'
    });
  }

  refreshStageData(): void {
    const allAssignments$ = this.stages.length
      ? forkJoin(this.stages.map(stage => this.eventOrganizationService.getStageResources(stage.binaId)))
      : of([] as StageResource[][]);

    allAssignments$.subscribe({
      next: stageResourceGroups => {
        this.allStageResources = stageResourceGroups.flat();
        this.stageResources = this.selectedStageId
          ? this.allStageResources.filter(item => item.stageId === this.selectedStageId)
          : [];
      },
      error: () => this.errorMessage = 'Unable to load stage resources.'
    });
  }

  selectStage(stageId: number): void {
    this.selectedStageId = stageId;
    this.resourceForm.patchValue({ stageId });
    this.stageResources = this.allStageResources.filter(item => item.stageId === stageId);
    this.clearMessages();
  }

  setTab(tab: MainTab): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  setResourceTab(tab: ResourceTab): void {
    this.activeTab = 'resources';
    this.activeResourceTab = tab;
    this.clearMessages();
  }

  openAddResource(): void {
    this.modalMode = 'add';
    this.editingStageResource = null;
    this.resourceForm.reset({
      name: '',
      type: 'Equipment',
      quantity: 1,
      stageId: this.selectedStageId,
      shareable: false,
      note: ''
    });
  }

  openEditResource(stageResource: StageResource): void {
    const resource = this.resources.find(item => item.id === stageResource.resourceId);
    this.modalMode = 'edit';
    this.editingStageResource = stageResource;
    this.resourceForm.reset({
      name: stageResource.resourceName,
      type: stageResource.resourceType,
      quantity: stageResource.quantity,
      stageId: stageResource.stageId,
      shareable: this.inventoryRows.find(row => row.resource.id === stageResource.resourceId)?.shared ?? false,
      note: resource?.description ?? ''
    });
  }

  closeResourceModal(): void {
    this.modalMode = null;
    this.editingStageResource = null;
    this.resourceForm.reset({
      name: '',
      type: 'Equipment',
      quantity: 1,
      stageId: this.selectedStageId,
      shareable: false,
      note: ''
    });
  }

  saveResource(): void {
    if (this.resourceForm.invalid) {
      this.resourceForm.markAllAsTouched();
      return;
    }

    const value = this.resourceForm.getRawValue();
    const stageId = Number(value.stageId);
    const quantity = Number(value.quantity);
    const resourceRequest = {
      name: value.name!,
      type: value.type!,
      description: value.note || null,
      totalQuantity: quantity
    };

    const operation = this.editingStageResource
      ? this.eventOrganizationService.updateResource(this.editingStageResource.resourceId, resourceRequest).pipe(
          switchMap(() => {
            const updateAssignment = this.eventOrganizationService.updateStageResource(
              this.editingStageResource!.stageId,
              this.editingStageResource!.resourceId,
              { resourceId: this.editingStageResource!.resourceId, quantity }
            );

            if (stageId === this.editingStageResource!.stageId) {
              return updateAssignment;
            }

            return updateAssignment.pipe(
              switchMap(() => this.eventOrganizationService.removeResourceFromStage(
                this.editingStageResource!.stageId,
                this.editingStageResource!.resourceId
              )),
              switchMap(() => this.eventOrganizationService.assignResourceToStage(stageId, {
                resourceId: this.editingStageResource!.resourceId,
                quantity
              }))
            );
          })
        )
      : this.eventOrganizationService.createResource(resourceRequest).pipe(
          switchMap(resource => this.eventOrganizationService.assignResourceToStage(stageId, {
            resourceId: resource.id,
            quantity
          }))
        );

    operation.subscribe({
      next: () => {
        this.successMessage = this.modalMode === 'edit' ? 'Resource updated.' : 'Resource added.';
        this.selectedStageId = stageId;
        this.closeResourceModal();
        this.reloadResourcesAndAssignments();
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to save resource.'
    });
  }

  confirmDelete(stageResource: StageResource): void {
    this.deletingStageResource = stageResource;
  }

  closeDeleteModal(): void {
    this.deletingStageResource = null;
  }

  deleteStageResource(): void {
    if (!this.deletingStageResource) return;

    this.eventOrganizationService.removeResourceFromStage(
      this.deletingStageResource.stageId,
      this.deletingStageResource.resourceId
    ).subscribe({
      next: () => {
        this.successMessage = 'Resource removed from stage.';
        this.closeDeleteModal();
        this.refreshStageData();
      },
      error: () => this.errorMessage = 'Unable to delete resource from stage.'
    });
  }

  updateInventorySearch(event: Event): void {
    this.inventorySearch = (event.target as HTMLInputElement).value;
  }

  updateInventoryStageFilter(event: Event): void {
    this.inventoryStageFilter = (event.target as HTMLSelectElement).value;
  }

  previousWeek(): void {
    this.timetableWeekOffset -= 1;
  }

  nextWeek(): void {
    this.timetableWeekOffset += 1;
  }

  logout(): void {
    this.authService.logout();
  }

  private reloadResourcesAndAssignments(): void {
    this.eventOrganizationService.getResources().subscribe({
      next: resources => {
        this.resources = resources;
        this.refreshStageData();
      },
      error: () => this.errorMessage = 'Unable to reload resources.'
    });
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private weekStartDate(): Date {
    const date = new Date();
    date.setHours(0, 0, 0, 0);
    date.setDate(date.getDate() + this.timetableWeekOffset * 7);

    const day = date.getDay();
    const mondayOffset = day === 0 ? -6 : 1 - day;
    date.setDate(date.getDate() + mondayOffset);
    return date;
  }

  private formatShortDate(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private formatLongDate(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }
}
