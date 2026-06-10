import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  EventReservationRequest,
  EventReservationStatus
} from '../../core/models/event-organization.model';
import { RequestFilter } from './event-organization.types';

@Component({
  selector: 'app-event-organization-requests-tab',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './requests-tab.component.html',
  styleUrls: ['./event-organization.shared.css', './requests-tab.component.css']
})
export class RequestsTabComponent {
  @Input({ required: true }) requestsSubtitle = '';
  @Input({ required: true }) requestSearch = '';
  @Input({ required: true }) activeRequestFilter: RequestFilter = 'All';
  @Input({ required: true }) reservationRequests: EventReservationRequest[] = [];
  @Input({ required: true }) requestResourceCounts: Record<number, number> = {};

  @Output() requestSearchChange = new EventEmitter<string>();
  @Output() viewReservation = new EventEmitter<EventReservationRequest>();

  updateRequestSearch(event: Event): void {
    this.requestSearchChange.emit((event.target as HTMLInputElement).value);
  }

  statusLabel(status: EventReservationStatus): string {
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

  isPastRequest(request: EventReservationRequest): boolean {
    return request.performanceDate < this.formatApiDate(new Date());
  }

  private formatApiDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
