import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-prodaja-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './prodaja-layout.component.html',
  styleUrls: ['./prodaja-layout.component.css']
})
export class ProdajaLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  currentUser: User | null = null;

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
  }

  logout(): void {
    this.authService.logout();
  }
}
