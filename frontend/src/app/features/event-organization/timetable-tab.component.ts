import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Stage } from '../../core/models/bina.model';
import {
  EventReservationRequest,
  TimetableSlot
} from '../../core/models/event-organization.model';
import { TimetableDay } from './event-organization.types';

@Component({
  selector: 'app-event-organization-timetable-tab',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timetable-tab.component.html',
  styleUrls: ['./event-organization.shared.css', './timetable-tab.component.css']
})
export class TimetableTabComponent {
  @Input({ required: true }) reservationMode = false;
  @Input({ required: true }) selectedReservationRequest: EventReservationRequest | null = null;
  @Input({ required: true }) reservationDateLabel = '';
  @Input({ required: true }) requestResourceCounts: Record<number, number> = {};
  @Input({ required: true }) stages: Stage[] = [];
  @Input({ required: true }) selectedStageId: number | null = null;
  @Input({ required: true }) timetableHours: string[] = [];
  @Input({ required: true }) timetableWeekLabel = '';
  @Input({ required: true }) timetableDays: TimetableDay[] = [];
  @Input({ required: true }) timetableSlots: Record<string, Record<string, TimetableSlot>> = {};
  @Input({ required: true }) selectedScheduleStart: string | null = null;
  @Input({ required: true }) selectedScheduleEnd: string | null = null;
  @Input({ required: true }) canReserveSelectedSlot = false;

  @Output() previousWeekRequested = new EventEmitter<void>();
  @Output() nextWeekRequested = new EventEmitter<void>();
  @Output() stageSelected = new EventEmitter<number>();
  @Output() scheduleStartSelected = new EventEmitter<string>();
  @Output() reserveSelectedSlotRequested = new EventEmitter<void>();

  getTimetableSlot(dayKey: string, hour: string): TimetableSlot | null {
    return this.timetableSlots[dayKey]?.[hour] ?? null;
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

  isScheduleSlotSelected(hour: string): boolean {
    return this.selectedScheduleStart === hour;
  }

  requestDurationMinutes(request: EventReservationRequest): number {
    const [startHours, startMinutes] = request.startTime.split(':').map(Number);
    const [endHours, endMinutes] = request.endTime.split(':').map(Number);
    return (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes);
  }

  private minutesFromTime(time: string): number {
    const [hours, minutes] = time.slice(0, 5).split(':').map(Number);
    return hours * 60 + minutes;
  }
}
