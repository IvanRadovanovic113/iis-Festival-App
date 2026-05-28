import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-negotiation-manager-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './negotiation-manager-layout.component.html',
  styleUrls: ['./negotiation-manager-layout.component.css']
})
export class NegotiationManagerLayoutComponent implements OnInit {
  currentManagerName = 'Manager';
  userInitials = 'M';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe({
      next: (user) => {
        if (user && user.username) {
          this.currentManagerName = user.username;
          this.generateInitials(this.currentManagerName);
        }
      },
      error: (err) => console.error('Failed to load user info in layout', err)
    });
  }

  private generateInitials(name: string): void {
    if (!name) return;
    const parts = name.trim().split(' ');
    if (parts.length > 1) {
      this.userInitials = (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    } else {
      this.userInitials = name.substring(0, 2).toUpperCase();
    }
  }

  onLogout(): void {
    this.authService.logout();
  }
}