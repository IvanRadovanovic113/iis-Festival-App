import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Stage } from '../../core/models/bina.model';
import { StageResource } from '../../core/models/event-organization.model';

@Component({
  selector: 'app-event-organization-delete-resource-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './delete-resource-modal.component.html',
  styleUrls: ['./event-organization.shared.css', './event-organization-modal.css']
})
export class DeleteResourceModalComponent {
  @Input({ required: true }) stageResource!: StageResource;
  @Input({ required: true }) stages: Stage[] = [];

  @Output() closeRequested = new EventEmitter<void>();
  @Output() deleteRequested = new EventEmitter<void>();

  stageNameById(stageId: number): string {
    return this.stages.find(stage => stage.stageId === stageId)?.name ?? 'this stage';
  }
}
