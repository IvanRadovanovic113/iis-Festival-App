import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Stage } from '../../core/models/bina.model';
import {
  EventResource,
  StageResource
} from '../../core/models/event-organization.model';
import { InventoryRow, ResourceTab } from './event-organization.types';

@Component({
  selector: 'app-event-organization-resources-tab',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resources-tab.component.html',
  styleUrls: ['./event-organization.shared.css', './resources-tab.component.css']
})
export class ResourcesTabComponent {
  @Input({ required: true }) activeResourceTab: ResourceTab = 'manage';
  @Input({ required: true }) stages: Stage[] = [];
  @Input({ required: true }) selectedStageId: number | null = null;
  @Input({ required: true }) selectedStage: Stage | null = null;
  @Input({ required: true }) stageResources: StageResource[] = [];
  @Input({ required: true }) resources: EventResource[] = [];
  @Input({ required: true }) inventoryRows: InventoryRow[] = [];
  @Input({ required: true }) inventorySearch = '';
  @Input({ required: true }) inventoryStageFilter = 'All';
  @Input({ required: true }) totalUnits = 0;
  @Input({ required: true }) sharedResourceCount = 0;

  @Output() stageSelected = new EventEmitter<number>();
  @Output() addResourceRequested = new EventEmitter<void>();
  @Output() editResourceRequested = new EventEmitter<StageResource>();
  @Output() deleteResourceRequested = new EventEmitter<StageResource>();
  @Output() inventorySearchChange = new EventEmitter<string>();
  @Output() inventoryStageFilterChange = new EventEmitter<string>();

  updateInventorySearch(event: Event): void {
    this.inventorySearchChange.emit((event.target as HTMLInputElement).value);
  }

  updateInventoryStageFilter(event: Event): void {
    this.inventoryStageFilterChange.emit((event.target as HTMLSelectElement).value);
  }
}
