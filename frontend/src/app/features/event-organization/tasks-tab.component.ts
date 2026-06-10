import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventOrganizationTask } from '../../core/models/event-organization.model';
import { TaskFilter } from './event-organization.types';

@Component({
  selector: 'app-event-organization-tasks-tab',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tasks-tab.component.html',
  styleUrls: ['./event-organization.shared.css', './event-organization-modal.css', './tasks-tab.component.css']
})
export class TasksTabComponent {
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) tasks: EventOrganizationTask[] = [];
  @Input({ required: true }) activeTaskFilter: TaskFilter = 'All';

  @Output() resolveTask = new EventEmitter<{ task: EventOrganizationTask; note: string }>();
  @Output() rejectTask = new EventEmitter<{ task: EventOrganizationTask; reason: string }>();

  taskToResolve: EventOrganizationTask | null = null;
  taskToReject: EventOrganizationTask | null = null;

  resolveForm = this.fb.group({
    note: ['']
  });

  rejectForm = this.fb.group({
    reason: ['', Validators.required]
  });

  get tasksSubtitle(): string {
    if (this.activeTaskFilter === 'OPEN') return 'Open tasks - resources unavailable or not in system';
    if (this.activeTaskFilter === 'RESOLVED') return 'Resolved tasks';
    if (this.activeTaskFilter === 'REJECTED') return 'Rejected tasks';
    return 'All tasks - resources unavailable or not in system';
  }

  openResolveModal(task: EventOrganizationTask): void {
    this.taskToResolve = task;
    this.resolveForm.reset({ note: '' });
  }

  closeResolveModal(): void {
    this.taskToResolve = null;
    this.resolveForm.reset({ note: '' });
  }

  submitResolve(): void {
    if (!this.taskToResolve) return;
    this.resolveTask.emit({
      task: this.taskToResolve,
      note: this.resolveForm.getRawValue().note?.trim() ?? ''
    });
    this.closeResolveModal();
  }

  openRejectModal(task: EventOrganizationTask): void {
    this.taskToReject = task;
    this.rejectForm.reset({ reason: '' });
  }

  closeRejectModal(): void {
    this.taskToReject = null;
    this.rejectForm.reset({ reason: '' });
  }

  submitReject(): void {
    if (!this.taskToReject) return;
    if (this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }
    this.rejectTask.emit({
      task: this.taskToReject,
      reason: this.rejectForm.getRawValue().reason!.trim()
    });
    this.closeRejectModal();
  }

  formatTaskDate(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}.${month}.${year}`;
  }

  formatChangedDate(date?: string): string {
    if (!date) return '';
    return this.formatTaskDate(date.slice(0, 10));
  }

  taskTypeLabel(task: EventOrganizationTask): string {
    return task.type === 'PROCUREMENT' ? 'Procurement' : 'Non-existing';
  }
}
