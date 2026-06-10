import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MainTab } from './event-organization.types';

@Component({
  selector: 'app-event-organization-placeholder-tab',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './placeholder-tab.component.html',
  styleUrls: ['./event-organization.shared.css']
})
export class PlaceholderTabComponent {
  @Input({ required: true }) activeTab: MainTab = 'tasks';
}
