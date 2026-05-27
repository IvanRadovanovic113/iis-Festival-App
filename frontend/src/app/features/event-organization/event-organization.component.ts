import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { BinaService } from '../../core/services/bina.service';
import { EventOrganizationService } from '../../core/services/event-organization.service';
import { Stage } from '../../core/models/bina.model';
import {
  EventReservationRequest,
  EventReservationStatus,
  EventResource,
  RequestResource,
  StageResource,
  TimetableSlot
} from '../../core/models/event-organization.model';
import { User } from '../../core/models/user.model';

type MainTab = 'requests' | 'timetable' | 'resources' | 'tasks' | 'analytics';
type ResourceTab = 'manage' | 'inventory';
type ResourceModalMode = 'add' | 'edit';
type RequestFilter = 'All' | 'PENDING' | 'APPROVED' | 'PAST';

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
  activeRequestFilter: RequestFilter = 'All';
  activeResourceTab: ResourceTab = 'manage';
  errorMessage = '';
  successMessage = '';
  requestSearch = '';

  stages: Stage[] = [];
  reservationRequests: EventReservationRequest[] = [];
  selectedReservationRequest: EventReservationRequest | null = null;
  selectedRequestResources: RequestResource[] = [];
  requestResourceCounts: Record<number, number> = {};
  resources: EventResource[] = [];
  stageResources: StageResource[] = [];
  allStageResources: StageResource[] = [];
  selectedStageId: number | null = null;
  timetableSlots: Record<string, Record<string, TimetableSlot>> = {};

  modalMode: ResourceModalMode | null = null;
  editingStageResource: StageResource | null = null;
  deletingStageResource: StageResource | null = null;

  inventorySearch = '';
  inventoryStageFilter = 'All';
  timetableWeekOffset = 0;
  timetableHours = ['14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'];

  reviewForm = this.fb.group({
    reviewNote: ['']
  });

  requestResourceForm = this.fb.group({
    resourceId: [null as number | null, Validators.required],
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

  stageNameById(stageId: number): string {
    return this.stages.find(stage => stage.stageId === stageId)?.name ?? 'this stage';
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.clearMessages();
    forkJoin({
      stages: this.stageService.getAll(),
      resources: this.eventOrganizationService.getResources(),
      requests: this.eventOrganizationService.getReservationRequests()
    }).subscribe({
      next: ({ stages, resources, requests }) => {
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
      },
      error: () => this.errorMessage = 'Unable to load event organization data.'
    });
  }

  refreshStageData(): void {
    const allAssignments$ = this.stages.length
      ? forkJoin(this.stages.map(stage => this.eventOrganizationService.getStageResources(stage.stageId)))
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
    if (this.activeTab === 'timetable') {
      this.loadTimetable();
    }
  }

  setTab(tab: MainTab): void {
    this.activeTab = tab;
    this.clearMessages();
    if (tab === 'timetable') {
      this.loadTimetable();
    }
  }

  setRequestFilter(filter: RequestFilter): void {
    this.activeRequestFilter = filter;
    this.selectedReservationRequest = this.filteredReservationRequests[0] ?? null;
  }

  updateRequestSearch(event: Event): void {
    this.requestSearch = (event.target as HTMLInputElement).value;
  }

  viewReservationRequest(request: EventReservationRequest): void {
    this.selectReservationRequest(request);
    this.selectedStageId = request.stageId;
    this.activeTab = 'timetable';
    this.loadTimetable();
  }

  selectReservationRequest(request: EventReservationRequest): void {
    this.selectedReservationRequest = request;
    this.reviewForm.reset({ reviewNote: request.reviewNote ?? '' });
    this.requestResourceForm.reset({ resourceId: this.resources[0]?.id ?? null, quantity: 1 });
    this.loadSelectedRequestResources();
    this.clearMessages();
  }

  approveSelectedRequest(): void {
    if (!this.selectedReservationRequest) return;

    const reviewNote = this.reviewForm.getRawValue().reviewNote || null;
    this.eventOrganizationService.approveReservationRequest(this.selectedReservationRequest.id, { reviewNote }).subscribe({
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

    const reviewNote = this.reviewForm.getRawValue().reviewNote || null;
    this.eventOrganizationService.rejectReservationRequest(this.selectedReservationRequest.id, { reviewNote }).subscribe({
      next: request => {
        this.successMessage = 'Reservation request rejected.';
        this.reloadReservationRequests(request.id);
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to reject reservation request.'
    });
  }

  addResourceToSelectedRequest(): void {
    if (!this.selectedReservationRequest) return;
    if (this.requestResourceForm.invalid) {
      this.requestResourceForm.markAllAsTouched();
      return;
    }

    const value = this.requestResourceForm.getRawValue();
    this.eventOrganizationService.addResourceToRequest(this.selectedReservationRequest.id, {
      resourceId: Number(value.resourceId),
      quantity: Number(value.quantity)
    }).subscribe({
      next: () => {
        this.successMessage = 'Resource requested for performance.';
        this.requestResourceForm.reset({ resourceId: this.resources[0]?.id ?? null, quantity: 1 });
        this.loadSelectedRequestResources();
      },
      error: err => this.errorMessage = err.error?.message || 'Unable to add resource to request.'
    });
  }

  confirmRequestResource(resource: RequestResource): void {
    if (!this.selectedReservationRequest) return;

    this.eventOrganizationService.confirmRequestResource(this.selectedReservationRequest.id, resource.resourceId).subscribe({
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

    this.eventOrganizationService.removeResourceFromRequest(this.selectedReservationRequest.id, resource.resourceId).subscribe({
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
    this.loadTimetable();
  }

  nextWeek(): void {
    this.timetableWeekOffset += 1;
    this.loadTimetable();
  }

  getTimetableSlot(dayKey: string, hour: string): TimetableSlot | null {
    return this.timetableSlots[dayKey]?.[hour] ?? null;
  }

  statusLabel(status: EventReservationStatus): string {
    return status.charAt(0) + status.slice(1).toLowerCase();
  }

  requestResourceStatusLabel(status: string): string {
    return status.charAt(0) + status.slice(1).toLowerCase();
  }

  requestStatusLabel(request: EventReservationRequest): string {
    if (this.isPastRequest(request)) return 'Past';
    if (request.status === 'APPROVED') return 'Confirmed';
    return this.statusLabel(request.status);
  }

  requestDurationMinutes(request: EventReservationRequest): number {
    const [startHours, startMinutes] = request.startTime.split(':').map(Number);
    const [endHours, endMinutes] = request.endTime.split(':').map(Number);
    return (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes);
  }

  formatRequestDate(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}.${month}.${year}`;
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

  private reloadReservationRequests(selectedRequestId?: number): void {
    this.eventOrganizationService.getReservationRequests().subscribe({
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

    this.eventOrganizationService.getRequestResources(this.selectedReservationRequest.id).subscribe({
      next: resources => this.selectedRequestResources = resources,
      error: () => this.errorMessage = 'Unable to load request resources.'
    });
  }

  private loadRequestResourceCounts(): void {
    if (!this.reservationRequests.length) {
      this.requestResourceCounts = {};
      return;
    }

    forkJoin(this.reservationRequests.map(request => this.eventOrganizationService.getRequestResources(request.id))).subscribe({
      next: resourceGroups => {
        this.requestResourceCounts = {};
        resourceGroups.forEach((resources, index) => {
          this.requestResourceCounts[this.reservationRequests[index].id] = resources.length;
        });
      },
      error: () => this.errorMessage = 'Unable to load request resource counts.'
    });
  }

  private loadTimetable(): void {
    if (!this.selectedStageId) return;

    const days = this.timetableDays;
    forkJoin(days.map(day => this.eventOrganizationService.getTimetable(this.selectedStageId!, day.key))).subscribe({
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

  private normalizeHour(time: string): string {
    return time.slice(0, 5);
  }

  isPastRequest(request: EventReservationRequest): boolean {
    return request.performanceDate < this.formatApiDate(new Date());
  }
}
