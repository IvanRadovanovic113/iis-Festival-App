import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, forkJoin, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { BinaService } from '../../core/services/bina.service';
import { EventReservationService } from '../../core/services/event-reservation.service';
import { EventOrganizationTaskService } from '../../core/services/event-organization-task.service';
import { EventResourceService } from '../../core/services/event-resource.service';
import { RequestResourceService } from '../../core/services/request-resource.service';
import { StageResourceService } from '../../core/services/stage-resource.service';
import { Stage } from '../../core/models/bina.model';
import {
  EventOrganizationTask,
  EventReservationRequest,
  EventResource,
  RequestResource,
  StageResource,
  TimetableSlot
} from '../../core/models/event-organization.model';
import { User } from '../../core/models/user.model';
import { ConfirmationModalComponent } from './confirmation-modal.component';
import { DeleteResourceModalComponent } from './delete-resource-modal.component';
import {
  InventoryRow,
  MainTab,
  RequestFilter,
  ResourceModalMode,
  ResourceRequestPayload,
  ResourceTab,
  TaskFilter,
  TimetableDay,
  TimetableMode
} from './event-organization.types';
import { PlaceholderTabComponent } from './placeholder-tab.component';
import { RequestsTabComponent } from './requests-tab.component';
import { ResourceModalComponent } from './resource-modal.component';
import { ResourcesTabComponent } from './resources-tab.component';
import { TasksTabComponent } from './tasks-tab.component';
import { TimetableTabComponent } from './timetable-tab.component';

interface InitialEventOrganizationData {
  stages: Stage[];
  resources: EventResource[];
  requests: EventReservationRequest[];
}

@Component({
  selector: 'app-event-organization',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RequestsTabComponent,
    ResourcesTabComponent,
    TasksTabComponent,
    TimetableTabComponent,
    PlaceholderTabComponent,
    ResourceModalComponent,
    ConfirmationModalComponent,
    DeleteResourceModalComponent
  ],
  templateUrl: './event-organization.component.html',
  styleUrls: ['./event-organization.shared.css', './event-organization.component.css']
})
export class EventOrganizationComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly stageService = inject(BinaService);
  private readonly eventReservationService = inject(EventReservationService);
  private readonly taskService = inject(EventOrganizationTaskService);
  private readonly eventResourceService = inject(EventResourceService);
  private readonly requestResourceService = inject(RequestResourceService);
  private readonly stageResourceService = inject(StageResourceService);
  private readonly fb = inject(FormBuilder);

  currentUser: User | null = null;
  activeTab: MainTab = 'requests';
  activeRequestFilter: RequestFilter = 'All';
  activeResourceTab: ResourceTab = 'manage';
  activeTaskFilter: TaskFilter = 'All';
  errorMessage = '';
  successMessage = '';
  requestSearch = '';
  timetableMode: TimetableMode = 'static';
  selectedScheduleStart: string | null = null;
  confirmedReservation: EventReservationRequest | null = null;
  confirmedReservationTasks: EventOrganizationTask[] = [];

  stages: Stage[] = [];
  reservationRequests: EventReservationRequest[] = [];
  selectedReservationRequest: EventReservationRequest | null = null;
  selectedRequestResources: RequestResource[] = [];
  requestResourceCounts: Record<number, number> = {};
  requestResourcesByRequestId: Record<number, RequestResource[]> = {};
  tasks: EventOrganizationTask[] = [];
  resources: EventResource[] = [];
  stageResources: StageResource[] = [];
  allStageResources: StageResource[] = [];
  selectedStageId: number | null = null;
  timetableSlots: Record<string, Record<string, TimetableSlot>> = {};

  modalMode: ResourceModalMode | null = null;
  editingStageResource: StageResource | null = null;
  deletingStageResource: StageResource | null = null;
  resourceModalError = '';

  inventorySearch = '';
  inventoryStageFilter = 'All';
  timetableWeekOffset = 0;
  timetableHours = ['14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'];

  requestResourceForm = this.fb.group({
    mode: ['existing' as 'existing' | 'custom'],
    resourceId: [null as number | null],
    requestedName: [''],
    requestedType: ['Equipment'],
    quantity: [1, [Validators.required, Validators.min(1)]]
  });

  resourceForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    type: ['Equipment', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    stageId: [null as number | null, Validators.required],
    shareable: [false],
    note: ['']
  });

  get selectedStage(): Stage | null {
    return this.stages.find(stage => stage.stageId === this.selectedStageId) ?? null;
  }

  get filteredReservationRequests(): EventReservationRequest[] {
    const query = this.requestSearch.trim().toLowerCase();
    return this.reservationRequests.filter(request => {
      const matchesSearch = !query
        || request.performerName.toLowerCase().includes(query)
        || request.stageName.toLowerCase().includes(query);
      if (!matchesSearch) return false;
      if (this.activeRequestFilter === 'All') return true;
      if (this.activeRequestFilter === 'PAST') return this.isPastRequest(request);
      if (this.activeRequestFilter === 'APPROVED') return request.status === 'APPROVED' && !this.isPastRequest(request);
      return request.status === this.activeRequestFilter;
    });
  }

  get requestsSubtitle(): string {
    if (this.activeRequestFilter === 'PENDING') return 'Requests awaiting review';
    if (this.activeRequestFilter === 'APPROVED') return 'Confirmed reservations with time slots';
    if (this.activeRequestFilter === 'PAST') return 'History of past reservations';
    return 'All incoming reservation requests';
  }

  get inventoryRows(): InventoryRow[] {
    const rows = this.resources.map(resource => {
      const assignments = this.allStageResources.filter(item => item.resourceId === resource.id);
      const stageNames = assignments
        .map(item => this.stages.find(stage => stage.stageId === item.stageId)?.name)
        .filter((name): name is string => Boolean(name));

      return {
        resource,
        assignedQuantity: assignments.reduce((sum, item) => sum + item.quantity, 0),
        stageNames,
        shared: resource.shareable
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

  get filteredTasks(): EventOrganizationTask[] {
    if (this.activeTaskFilter === 'All') return this.tasks;
    return this.tasks.filter(task => task.status === this.activeTaskFilter);
  }

  get timetableDays(): TimetableDay[] {
    const start = this.weekStartDate();

    return ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((dayName, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);

      return {
        key: this.formatApiDate(date),
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

  get reservationMode(): boolean {
    return this.activeTab === 'timetable'
      && this.timetableMode === 'reservation'
      && this.selectedReservationRequest?.status === 'PENDING';
  }

  get reservationDateLabel(): string {
    if (!this.selectedReservationRequest) return '';
    return this.formatLongDate(this.parseApiDate(this.selectedReservationRequest.performanceDate));
  }

  get selectedScheduleEnd(): string | null {
    if (!this.selectedReservationRequest || !this.selectedScheduleStart) return null;
    return this.addMinutes(this.selectedScheduleStart, this.requestDurationMinutes(this.selectedReservationRequest));
  }

  get canReserveSelectedSlot(): boolean {
    return Boolean(this.selectedScheduleStart && this.isScheduleSlotAvailable(this.selectedScheduleStart));
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.clearMessages();
    forkJoin({
      stages: this.stageService.getAll(),
      resources: this.eventResourceService.getResources(),
      requests: this.eventReservationService.getReservationRequests()
    }).subscribe({
      next: ({ stages, resources, requests }: InitialEventOrganizationData) => {
        this.stages = stages;
        this.resources = resources;
        this.reservationRequests = requests;
        this.selectedReservationRequest = requests[0] ?? null;
        this.selectedStageId = stages[0]?.stageId ?? null;
        this.requestResourceForm.patchValue({ resourceId: resources[0]?.id ?? null });
        this.resourceForm.patchValue({ stageId: this.selectedStageId });
        this.refreshStageData();
        this.loadRequestResourceCounts();
        this.loadSelectedRequestResources();
        this.loadTasks();
      },
      error: () => this.errorMessage = 'Unable to load event organization data.'
    });
  }

  refreshStageData(): void {
    const allAssignments$ = this.stages.length
      ? forkJoin(this.stages.map(stage => this.stageResourceService.getStageResources(stage.stageId)))
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
    if (this.reservationMode && stageId !== this.selectedReservationRequest?.stageId) {
      return;
    }
    this.selectedStageId = stageId;
    this.resourceForm.patchValue({ stageId });
    this.stageResources = this.allStageResources.filter(item => item.stageId === stageId);
    this.clearMessages();
    if (this.activeTab === 'timetable') {
      this.loadTimetable();
    }
  }

  setTab(tab: MainTab): void {
    this.activeTab = tab;
    if (tab === 'timetable') {
      this.timetableMode = 'static';
      this.selectedReservationRequest = null;
      this.selectedScheduleStart = null;
    }
    this.clearMessages();
    if (tab === 'timetable') {
      this.loadTimetable();
    }
    if (tab === 'tasks') {
      this.loadTasks();
    }
  }

  setRequestFilter(filter: RequestFilter): void {
    this.activeRequestFilter = filter;
    this.selectedReservationRequest = this.filteredReservationRequests[0] ?? null;
  }

  viewReservationRequest(request: EventReservationRequest): void {
    this.selectReservationRequest(request);
    this.selectedStageId = request.stageId;
    this.activeTab = 'timetable';
    this.timetableMode = 'reservation';
    this.selectedScheduleStart = this.normalizeHour(request.startTime);
    this.setTimetableWeekToDate(request.performanceDate);
    this.loadTimetable();
  }

  selectReservationRequest(request: EventReservationRequest): void {
    this.selectedReservationRequest = request;
    this.resetRequestResourceForm();
    this.loadSelectedRequestResources();
    this.clearMessages();
  }

  approveSelectedRequest(): void {
    if (!this.selectedReservationRequest) return;

    this.eventReservationService.approveReservationRequest(this.selectedReservationRequest.id).subscribe({
      next: request => {
        this.successMessage = 'Reservation request approved.';
        this.reloadReservationRequests(request.id);
        if (this.activeTab === 'timetable') {
          this.loadTimetable();
        }
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to approve reservation request.'
    });
  }

  rejectSelectedRequest(): void {
    if (!this.selectedReservationRequest) return;

    this.eventReservationService.rejectReservationRequest(this.selectedReservationRequest.id).subscribe({
      next: request => {
        this.successMessage = 'Reservation request rejected.';
        this.reloadReservationRequests(request.id);
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to reject reservation request.'
    });
  }

  addResourceToSelectedRequest(): void {
    if (!this.selectedReservationRequest) return;

    const value = this.requestResourceForm.getRawValue();
    const quantity = Number(value.quantity);
    const payload = value.mode === 'custom'
      ? {
          resourceId: null,
          requestedName: value.requestedName?.trim() || null,
          requestedType: value.requestedType?.trim() || null,
          quantity
        }
      : {
          resourceId: value.resourceId == null ? null : Number(value.resourceId),
          quantity
        };

    if (this.requestResourceForm.invalid || (value.mode === 'existing' && !payload.resourceId) || (value.mode === 'custom' && (!payload.requestedName || !payload.requestedType))) {
      this.requestResourceForm.markAllAsTouched();
      this.errorMessage = 'Please enter the requested resource details.';
      return;
    }

    this.requestResourceService.addResourceToRequest(this.selectedReservationRequest.id, payload).subscribe({
      next: () => {
        this.successMessage = 'Resource requested for performance.';
        this.resetRequestResourceForm();
        this.loadSelectedRequestResources();
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to add resource to request.'
    });
  }

  confirmRequestResource(resource: RequestResource): void {
    if (!this.selectedReservationRequest) return;

    this.requestResourceService.confirmRequestResource(this.selectedReservationRequest.id, resource.id).subscribe({
      next: () => {
        this.successMessage = 'Resource availability confirmed.';
        this.loadSelectedRequestResources();
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Unable to confirm resource availability.';
        this.loadSelectedRequestResources();
      }
    });
  }

  removeRequestResource(resource: RequestResource): void {
    if (!this.selectedReservationRequest) return;

    this.requestResourceService.removeResourceFromRequest(this.selectedReservationRequest.id, resource.id).subscribe({
      next: () => {
        this.successMessage = 'Resource removed from request.';
        this.loadSelectedRequestResources();
      },
      error: () => this.errorMessage = 'Unable to remove resource from request.'
    });
  }

  setResourceTab(tab: ResourceTab): void {
    this.activeTab = 'resources';
    this.activeResourceTab = tab;
    this.clearMessages();
  }

  setTaskFilter(filter: TaskFilter): void {
    this.activeTab = 'tasks';
    this.activeTaskFilter = filter;
    this.clearMessages();
  }

  resolveTask(event: { task: EventOrganizationTask; note: string }): void {
    this.taskService.resolveTask(event.task.id, { note: event.note || null }).subscribe({
      next: () => {
        this.successMessage = 'Task marked as resolved.';
        this.loadTasks();
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to resolve task.'
    });
  }

  rejectTask(event: { task: EventOrganizationTask; reason: string }): void {
    this.taskService.rejectTask(event.task.id, { reason: event.reason }).subscribe({
      next: () => {
        this.successMessage = 'Task rejected.';
        this.loadTasks();
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to reject task.'
    });
  }

  openAddResource(): void {
    this.modalMode = 'add';
    this.editingStageResource = null;
    this.resourceModalError = '';
    this.clearMessages();
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
    this.resourceModalError = '';
    this.clearMessages();
    this.resourceForm.reset({
      name: stageResource.resourceName,
      type: stageResource.resourceType,
      quantity: stageResource.quantity,
      stageId: stageResource.stageId,
      shareable: resource?.shareable ?? false,
      note: resource?.description ?? ''
    });
  }

  closeResourceModal(): void {
    this.modalMode = null;
    this.editingStageResource = null;
    this.resourceModalError = '';
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
    const resourceName = value.name!.trim();
    const resourceType = value.type!;
    const shareable = Boolean(value.shareable);
    if (this.resourceNameExistsOnStage(resourceName, stageId, this.editingStageResource?.resourceId)) {
      this.resourceModalError = 'A resource with this name already exists on this stage.';
      return;
    }
    this.resourceModalError = '';

    const resourceRequest = {
      name: resourceName,
      type: resourceType,
      description: value.note || null,
      totalQuantity: quantity,
      shareable
    };

    const matchingResource = this.findMatchingResource(resourceName, resourceType, shareable, this.editingStageResource?.resourceId);
    const operation = this.editingStageResource
      ? this.saveEditedResource(stageId, quantity, resourceRequest, matchingResource)
      : this.saveNewResource(stageId, quantity, resourceRequest, matchingResource);

    operation.subscribe({
      next: () => {
        this.successMessage = this.modalMode === 'edit' ? 'Resource updated.' : 'Resource added.';
        this.selectedStageId = stageId;
        this.closeResourceModal();
        this.reloadResourcesAndAssignments();
      },
      error: err => this.resourceModalError = err.error?.message || 'Unable to save resource.'
    });
  }

  private resourceNameExistsOnStage(resourceName: string, stageId: number, ignoredResourceId?: number): boolean {
    const normalizedName = resourceName.trim().toLowerCase();
    return this.allStageResources.some(resource =>
      resource.stageId === stageId
      && resource.resourceId !== ignoredResourceId
      && resource.resourceName.trim().toLowerCase() === normalizedName
    );
  }

  private saveNewResource(
    stageId: number,
    quantity: number,
    resourceRequest: ResourceRequestPayload,
    matchingResource: EventResource | null
  ): Observable<StageResource> {
    const resource$ = matchingResource
      ? of(matchingResource)
      : this.eventResourceService.createResource(resourceRequest);

    return resource$.pipe(
      switchMap(resource => this.stageResourceService.assignResourceToStage(stageId, {
        resourceId: resource.id,
        quantity
      }))
    );
  }

  private saveEditedResource(
    stageId: number,
    quantity: number,
    resourceRequest: ResourceRequestPayload,
    matchingResource: EventResource | null
  ): Observable<StageResource> {
    const currentAssignment = this.editingStageResource!;
    const targetResourceId = matchingResource?.id ?? currentAssignment.resourceId;

    if (targetResourceId !== currentAssignment.resourceId) {
      return this.stageResourceService.removeResourceFromStage(currentAssignment.stageId, currentAssignment.resourceId).pipe(
        switchMap(() => this.stageResourceService.assignResourceToStage(stageId, {
          resourceId: targetResourceId,
          quantity
        }))
      );
    }

    return this.eventResourceService.updateResource(currentAssignment.resourceId, resourceRequest).pipe(
      switchMap(() => {
        const updateAssignment = this.stageResourceService.updateStageResource(
          currentAssignment.stageId,
          currentAssignment.resourceId,
          { resourceId: currentAssignment.resourceId, quantity }
        );

        if (stageId === currentAssignment.stageId) {
          return updateAssignment;
        }

        return updateAssignment.pipe(
          switchMap(() => this.stageResourceService.removeResourceFromStage(
            currentAssignment.stageId,
            currentAssignment.resourceId
          )),
          switchMap(() => this.stageResourceService.assignResourceToStage(stageId, {
            resourceId: currentAssignment.resourceId,
            quantity
          }))
        );
      })
    );
  }

  private findMatchingResource(name: string, type: string, shareable: boolean, ignoredResourceId?: number): EventResource | null {
    const normalizedName = name.trim().toLowerCase();
    const normalizedType = type.trim().toLowerCase();
    return this.resources.find(resource =>
      resource.id !== ignoredResourceId
      && resource.name.trim().toLowerCase() === normalizedName
      && resource.type.trim().toLowerCase() === normalizedType
      && resource.shareable === shareable
    ) ?? null;
  }

  confirmDelete(stageResource: StageResource): void {
    this.deletingStageResource = stageResource;
  }

  closeDeleteModal(): void {
    this.deletingStageResource = null;
  }

  deleteStageResource(): void {
    if (!this.deletingStageResource) return;

    this.stageResourceService.removeResourceFromStage(
      this.deletingStageResource.stageId,
      this.deletingStageResource.resourceId
    ).subscribe({
      next: () => {
        this.successMessage = 'Resource removed from stage.';
        this.closeDeleteModal();
        this.reloadResourcesAndAssignments();
      },
      error: () => this.errorMessage = 'Unable to remove resource from stage.'
    });
  }

  previousWeek(): void {
    this.timetableWeekOffset -= 1;
    this.loadTimetable();
  }

  nextWeek(): void {
    this.timetableWeekOffset += 1;
    this.loadTimetable();
  }

  selectScheduleStart(hour: string): void {
    if (!this.reservationMode || !this.isScheduleSlotAvailable(hour)) return;
    this.selectedScheduleStart = hour;
    this.clearMessages();
  }

  reserveSelectedSlot(): void {
    if (!this.selectedReservationRequest || !this.selectedScheduleStart) return;
    if (!this.isScheduleSlotAvailable(this.selectedScheduleStart)) {
      this.errorMessage = 'Selected slot is no longer available.';
      return;
    }

    this.eventReservationService.scheduleReservationRequest(this.selectedReservationRequest.id, {
      startTime: this.selectedScheduleStart
    }).subscribe({
      next: request => {
        this.confirmedReservation = request;
        this.confirmedReservationTasks = [];
        this.successMessage = 'Reservation confirmed.';
        this.timetableMode = 'static';
        this.selectedScheduleStart = null;
        this.reloadReservationRequests(request.id);
        this.loadTimetable();
        this.loadTasksForConfirmation(request.id);
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to reserve selected time slot.'
    });
  }

  closeConfirmation(): void {
    this.confirmedReservation = null;
    this.confirmedReservationTasks = [];
  }

  viewConfirmedReservationTasks(): void {
    this.closeConfirmation();
    this.activeTab = 'tasks';
    this.activeTaskFilter = 'OPEN';
    this.loadTasks();
  }

  isScheduleSlotAvailable(hour: string): boolean {
    if (!this.selectedReservationRequest) return false;
    const start = this.minutesFromTime(hour);
    const duration = this.requestDurationMinutes(this.selectedReservationRequest);
    const end = start + duration;
    if (end > 24 * 60) return false;

    const daySlots = Object.values(this.timetableSlots[this.selectedReservationRequest.performanceDate] ?? {});
    return !daySlots.some(slot => {
      if (slot.status !== 'OCCUPIED') return false;
      const slotStart = this.minutesFromTime(slot.startTime);
      const rawSlotEnd = this.minutesFromTime(slot.endTime);
      const slotEnd = rawSlotEnd <= slotStart ? 24 * 60 : rawSlotEnd;
      return slotStart < end && slotEnd > start;
    });
  }

  requestDurationMinutes(request: EventReservationRequest): number {
    const [startHours, startMinutes] = request.startTime.split(':').map(Number);
    const [endHours, endMinutes] = request.endTime.split(':').map(Number);
    return (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes);
  }

  logout(): void {
    this.authService.logout();
  }

  private reloadResourcesAndAssignments(): void {
    this.eventResourceService.getResources().subscribe({
      next: resources => {
        this.resources = resources;
        this.refreshStageData();
      },
      error: () => this.errorMessage = 'Unable to reload resources.'
    });
  }

  private reloadReservationRequests(selectedRequestId?: number): void {
    this.eventReservationService.getReservationRequests().subscribe({
      next: requests => {
        this.reservationRequests = requests;
        this.selectedReservationRequest = requests.find(request => request.id === selectedRequestId)
          ?? this.filteredReservationRequests[0]
          ?? null;
        this.loadRequestResourceCounts();
        this.loadSelectedRequestResources();
      },
      error: () => this.errorMessage = 'Unable to reload reservation requests.'
    });
  }

  private loadSelectedRequestResources(): void {
    if (!this.selectedReservationRequest) {
      this.selectedRequestResources = [];
      return;
    }

    this.requestResourceService.getRequestResources(this.selectedReservationRequest.id).subscribe({
      next: resources => {
        this.selectedRequestResources = resources;
        this.requestResourcesByRequestId[this.selectedReservationRequest!.id] = resources;
      },
      error: () => this.errorMessage = 'Unable to load request resources.'
    });
  }

  private resetRequestResourceForm(): void {
    this.requestResourceForm.reset({
      mode: 'existing',
      resourceId: this.resources[0]?.id ?? null,
      requestedName: '',
      requestedType: 'Equipment',
      quantity: 1
    });
  }

  private loadRequestResourceCounts(): void {
    if (!this.reservationRequests.length) {
      this.requestResourceCounts = {};
      return;
    }

    forkJoin(this.reservationRequests.map(request => this.requestResourceService.getRequestResources(request.id))).subscribe({
      next: resourceGroups => {
        this.requestResourceCounts = {};
        this.requestResourcesByRequestId = {};
        resourceGroups.forEach((resources, index) => {
          const requestId = this.reservationRequests[index].id;
          this.requestResourceCounts[requestId] = resources.length;
          this.requestResourcesByRequestId[requestId] = resources;
        });
      },
      error: () => this.errorMessage = 'Unable to load request resource counts.'
    });
  }

  private loadTasks(): void {
    this.taskService.getTasks().subscribe({
      next: tasks => this.tasks = tasks,
      error: () => this.errorMessage = 'Unable to load tasks.'
    });
  }

  private loadTasksForConfirmation(reservationRequestId: number): void {
    this.taskService.getTasks().subscribe({
      next: tasks => {
        this.tasks = tasks;
        this.confirmedReservationTasks = tasks.filter(task => task.reservationRequestId === reservationRequestId);
      },
      error: () => {
        this.confirmedReservationTasks = [];
        this.errorMessage = 'Reservation confirmed, but tasks could not be loaded.';
      }
    });
  }

  private loadTimetable(): void {
    if (!this.selectedStageId) return;

    const days = this.timetableDays;
    forkJoin(days.map(day => this.eventReservationService.getTimetable(this.selectedStageId!, day.key))).subscribe({
      next: slotGroups => {
        this.timetableSlots = {};
        slotGroups.forEach((slots, index) => {
          const dayKey = days[index].key;
          this.timetableSlots[dayKey] = {};
          slots.forEach(slot => {
            this.timetableSlots[dayKey][this.normalizeHour(slot.startTime)] = slot;
          });
        });
      },
      error: () => this.errorMessage = 'Unable to load timetable.'
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

  private setTimetableWeekToDate(date: string): void {
    const targetWeekStart = this.weekStartForDate(this.parseApiDate(date));
    const currentWeekStart = this.weekStartDate();
    const diffMs = targetWeekStart.getTime() - currentWeekStart.getTime();
    this.timetableWeekOffset += Math.round(diffMs / (7 * 24 * 60 * 60 * 1000));
  }

  private weekStartForDate(date: Date): Date {
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    const day = start.getDay();
    const mondayOffset = day === 0 ? -6 : 1 - day;
    start.setDate(start.getDate() + mondayOffset);
    return start;
  }

  private formatShortDate(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private formatLongDate(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  private formatApiDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private parseApiDate(date: string): Date {
    const [year, month, day] = date.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private normalizeHour(time: string): string {
    return time.slice(0, 5);
  }

  private minutesFromTime(time: string): number {
    const [hours, minutes] = time.slice(0, 5).split(':').map(Number);
    return hours * 60 + minutes;
  }

  private addMinutes(time: string, minutesToAdd: number): string {
    const total = this.minutesFromTime(time) + minutesToAdd;
    const hours = Math.floor(total / 60);
    const minutes = total % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
  }

  isPastRequest(request: EventReservationRequest): boolean {
    return request.performanceDate < this.formatApiDate(new Date());
  }
}
