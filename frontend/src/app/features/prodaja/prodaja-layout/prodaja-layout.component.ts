import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
  private destroyRef = inject(DestroyRef);
  currentUser: User | null = null;

  ngOnInit(): void {
    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => this.currentUser = user);
  }

  logout(): void {
    this.authService.logout();
  }
}
