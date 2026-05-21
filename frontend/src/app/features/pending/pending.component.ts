import { Component, OnInit, inject } from '@angular/core';
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

  currentUser: User | null = null;
  refreshing = false;

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
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
