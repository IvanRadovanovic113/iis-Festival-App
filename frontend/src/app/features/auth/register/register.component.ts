import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    customer: [false],
    fullName: ['']
  });

  error = '';
  loading = false;
  success = false;

  get username() { return this.form.get('username')!; }
  get email() { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }
  get customer() { return this.form.get('customer')!; }
  get fullName() { return this.form.get('fullName')!; }
  get isCustomer(): boolean { return this.customer.value === true; }

  onCustomerToggle(): void {
    if (this.isCustomer) {
      this.fullName.setValidators([Validators.required, Validators.minLength(2)]);
    } else {
      this.fullName.clearValidators();
      this.fullName.setValue('');
    }
    this.fullName.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    const payload: RegisterRequest = {
      username: this.form.value.username!,
      email: this.form.value.email!,
      password: this.form.value.password!,
      customer: this.isCustomer,
      fullName: this.isCustomer ? this.form.value.fullName! : undefined
    };
    this.authService.register(payload).subscribe({
      next: () => {
        this.success = true;
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Registration error.';
        this.loading = false;
      }
    });
  }
}
