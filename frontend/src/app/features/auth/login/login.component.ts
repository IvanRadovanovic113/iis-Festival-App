import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  error = '';
  loading = false;

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.authService.login(this.form.value as LoginRequest).subscribe({
      next: (response) => {
        if (response.user.role === 'ADMIN') {
          this.router.navigate(['/dashboard']);
        } else if (response.user.assignment?.festivalRole === 'EVENT_ORGANIZER' || response.user.role === 'EVENT_ORGANIZER') {
          this.router.navigate(['/event-organization']);
        } else {
          this.router.navigate(['/pending']);
        }
      },
      error: () => {
        this.error = 'Pogresno korisnicko ime ili lozinka.';
        this.loading = false;
      }
    });
  }
}
