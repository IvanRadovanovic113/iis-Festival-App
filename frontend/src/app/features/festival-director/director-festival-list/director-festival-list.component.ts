import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { FestivalCampaignOverview } from '../../../core/models/campaign.model';
import { FESTIVAL_STATUS_LABELS, FestivalStatus } from '../../../core/models/festival.model';

@Component({
  selector: 'app-director-festival-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './director-festival-list.component.html',
  styleUrls: ['./director-festival-list.component.css']
})
export class DirectorFestivalListComponent implements OnInit {
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);

  festivals: FestivalCampaignOverview[] = [];
  filteredFestivals: FestivalCampaignOverview[] = [];
  errorMessage = '';
  statusLabels = FESTIVAL_STATUS_LABELS;
  currentUser = this.authService.getCurrentUser();
  search = '';
  dateFrom = '';
  dateTo = '';
  selectedStatus = '';
  sortBy = 'startDateAsc';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getDirectorFestivalOverviews().subscribe({
      next: festivals => {
        this.festivals = festivals;
        this.applyFilters();
      },
      error: () => this.errorMessage = 'Error loading festivals.'
    });
  }

  get displayName(): string {
    return this.currentUser?.username || 'User';
  }

  get avatarLabel(): string {
    const name = this.displayName.trim();
    const parts = name.split(/[._-]+/).filter(Boolean);
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }

  get statusOptions(): Array<{ value: string; label: string }> {
    return Array.from(new Set(this.festivals.map(festival => festival.status)))
      .map(status => ({
        value: status,
        label: this.getStatusLabel(status)
      }))
      .sort((first, second) => first.label.localeCompare(second.label));
  }

  applyFilters(): void {
    const term = this.search.trim().toLowerCase();
    const fromDate = this.dateFrom ? new Date(this.dateFrom) : null;
    const toDate = this.dateTo ? new Date(this.dateTo) : null;

    this.filteredFestivals = this.festivals
      .filter(festival => {
        const matchesSearch = !term
          || festival.name.toLowerCase().includes(term)
          || festival.location.toLowerCase().includes(term)
          || (festival.campaignName ?? '').toLowerCase().includes(term);
        const matchesStatus = !this.selectedStatus || festival.status === this.selectedStatus;
        const festivalStart = new Date(festival.startDate);
        const festivalEnd = new Date(festival.endDate);
        const matchesFrom = !fromDate || festivalStart >= fromDate;
        const matchesTo = !toDate || festivalEnd <= toDate;
        return matchesSearch && matchesStatus && matchesFrom && matchesTo;
      })
      .sort((first, second) => {
        switch (this.sortBy) {
          case 'startDateDesc':
            return new Date(second.startDate).getTime() - new Date(first.startDate).getTime();
          case 'nameAsc':
            return first.name.localeCompare(second.name);
          case 'nameDesc':
            return second.name.localeCompare(first.name);
          default:
            return new Date(first.startDate).getTime() - new Date(second.startDate).getTime();
        }
      });
  }

  resetFilters(): void {
    this.search = '';
    this.dateFrom = '';
    this.dateTo = '';
    this.selectedStatus = '';
    this.sortBy = 'startDateAsc';
    this.applyFilters();
  }

  getStatusLabel(status: string): string {
    return this.statusLabels[status as FestivalStatus] ?? status;
  }

  logout(): void {
    this.authService.logout();
  }
}
