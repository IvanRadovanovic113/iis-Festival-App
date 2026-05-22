import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FestivalService } from '../../../core/services/festival.service';
import { AuthService } from '../../../core/services/auth.service';
import { Festival, FESTIVAL_STATUS_LABELS } from '../../../core/models/festival.model';

@Component({
  selector: 'app-festival-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './festival-list.component.html',
  styleUrls: ['./festival-list.component.css']
})
export class FestivalListComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private authService = inject(AuthService);

  festivals: Festival[] = [];
  statusLabels = FESTIVAL_STATUS_LABELS;
  errorMessage = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.festivalService.getAll().subscribe({
      next: data => this.festivals = data,
      error: () => this.errorMessage = 'Error loading festivals.'
    });
  }

  delete(festival: Festival): void {
    if (!confirm(`Are you sure you want to delete festival "${festival.name}"?`)) return;
    this.festivalService.delete(festival.festivalId).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage = 'Error deleting festival.'
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
