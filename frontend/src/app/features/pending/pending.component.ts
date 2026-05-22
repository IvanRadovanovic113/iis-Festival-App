import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-pending',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pending.component.html',
  styleUrls: ['./pending.component.css']
})
export class PendingComponent implements OnInit {
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  currentUser: User | null = null;
  refreshing = false;

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => this.currentUser = user);
  }

  refresh(): void {
    this.refreshing = true;
    this.authService.refreshCurrentUser().subscribe({
      next: () => { this.refreshing = false; },
      error: () => { this.refreshing = false; }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
