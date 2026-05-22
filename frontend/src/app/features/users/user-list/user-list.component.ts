import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AssignmentService } from '../../../core/services/assignment.service';
import { FestivalService } from '../../../core/services/festival.service';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { Festival } from '../../../core/models/festival.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  private assignmentService = inject(AssignmentService);
  private festivalService = inject(FestivalService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  users: User[] = [];
  festivals: Festival[] = [];
  roles: string[] = [];
  errorMessage = '';

  selectedUser: User | null = null;

  form = this.fb.group({
    festivalId: [null as number | null, Validators.required],
    role: ['', Validators.required]
  });

  ngOnInit(): void {
    this.load();
    this.festivalService.getAll().subscribe({ next: f => this.festivals = f });
    this.assignmentService.getRoles().subscribe({ next: r => this.roles = r });
  }

  load(): void {
    this.errorMessage = '';
    this.assignmentService.getUsers().subscribe({
      next: data => this.users = data,
      error: () => this.errorMessage = 'Error loading users.'
    });
  }

  openAssign(user: User): void {
    this.selectedUser = user;
    this.form.reset({
      festivalId: user.assignment?.festivalId ?? null,
      role: user.assignment?.festivalRole ?? ''
    });
  }

  cancelAssign(): void {
    this.selectedUser = null;
    this.form.reset();
  }

  saveAssign(): void {
    if (this.form.invalid || !this.selectedUser) {
      this.form.markAllAsTouched();
      return;
    }
    const body = {
      festivalId: this.form.value.festivalId!,
      role: this.form.value.role!
    };
    const request = this.selectedUser.assignment
      ? this.assignmentService.updateAssignment(this.selectedUser.id, body)
      : this.assignmentService.assign(this.selectedUser.id, body);

    request.subscribe({
      next: () => {
        this.selectedUser = null;
        this.form.reset();
        this.load();
      },
      error: () => this.errorMessage = 'Error saving assignment.'
    });
  }

  deleteAssignment(user: User): void {
    if (!confirm(`Remove assignment for user "${user.username}"?`)) return;
    this.assignmentService.deleteAssignment(user.id).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage = 'Error removing assignment.'
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
