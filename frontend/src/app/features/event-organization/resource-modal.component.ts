import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Stage } from '../../core/models/bina.model';
import { ResourceModalMode } from './event-organization.types';

@Component({
  selector: 'app-event-organization-resource-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './resource-modal.component.html',
  styleUrls: ['./event-organization.shared.css', './event-organization-modal.css']
})
export class ResourceModalComponent {
  @Input({ required: true }) modalMode: ResourceModalMode = 'add';
  @Input({ required: true }) resourceForm!: FormGroup;
  @Input({ required: true }) stages: Stage[] = [];
  @Input({ required: true }) resourceModalError = '';

  @Output() closeRequested = new EventEmitter<void>();
  @Output() saveRequested = new EventEmitter<void>();
}
