import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  EventOrganizationTask,
  EventReservationRequest
} from '../../core/models/event-organization.model';

@Component({
  selector: 'app-event-organization-confirmation-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./event-organization.shared.css', './event-organization-modal.css']
})
export class ConfirmationModalComponent {
  @Input({ required: true }) reservation!: EventReservationRequest;
  @Input({ required: true }) requestResourceCounts: Record<number, number> = {};
  @Input() tasks: EventOrganizationTask[] = [];

  @Output() closeRequested = new EventEmitter<void>();
  @Output() viewTasksRequested = new EventEmitter<void>();

  get hasTasks(): boolean {
    return this.tasks.length > 0;
  }

  formatRequestDate(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}.${month}.${year}`;
  }

  formatLongDate(date: string): string {
    const [year, month, day] = date.split('-').map(Number);
    return new Date(year, month - 1, day).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric'
    });
  }

  formatTaskDate(date: string): string {
    return this.formatRequestDate(date);
  }

  taskTypeLabel(task: EventOrganizationTask): string {
    return task.type === 'PROCUREMENT' ? 'Procurement' : 'Non-existing';
  }

  taskReason(task: EventOrganizationTask): string {
    return task.type === 'PROCUREMENT'
      ? 'Resource unavailable for procurement from other stages'
      : 'Resource does not exist in system';
  }
}
