import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { TicketType } from '../../../../core/models/ticket-type.model';
import { User } from '../../../../core/models/user.model';

@Component({
  selector: 'app-ticket-type-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ticket-type-list.component.html',
  styleUrls: ['./ticket-type-list.component.css']
})
export class TicketTypeListComponent implements OnInit {
  private authService = inject(AuthService);
  private ticketTypeService = inject(TicketTypeService);
  private destroyRef = inject(DestroyRef);

  currentUser: User | null = null;
  ticketTypes: TicketType[] = [];
  errorMessage = '';

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => {
      this.currentUser = user;
      if (user?.assignment?.festivalId) {
        this.load();
      }
    });
  }

  load(): void {
    this.errorMessage = '';
    this.ticketTypeService.getAll(this.currentUser!.assignment!.festivalId).subscribe({
      next: data => this.ticketTypes = data,
      error: () => this.errorMessage = 'Error loading ticket types.'
    });
  }

  delete(tt: TicketType): void {
    if (!confirm(`Delete ticket type "${tt.name}"?`)) return;
    this.ticketTypeService.delete(tt.ticketTypeId).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage = err.error?.message || 'Error deleting ticket type.'
    });
  }

  soldPercent(tt: TicketType): number {
    return tt.totalQuantity > 0 ? (tt.soldCount / tt.totalQuantity) * 100 : 0;
  }
}
