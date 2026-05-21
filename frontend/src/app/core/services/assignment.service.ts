import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/admin';

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.BASE}/users`);
  }

  getRoles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.BASE}/roles`);
  }

  assign(userId: number, body: { festivalId: number; role: string }): Observable<User> {
    return this.http.post<User>(`${this.BASE}/users/${userId}/assignment`, body);
  }

  updateAssignment(userId: number, body: { festivalId: number; role: string }): Observable<User> {
    return this.http.put<User>(`${this.BASE}/users/${userId}/assignment`, body);
  }

  deleteAssignment(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/users/${userId}/assignment`);
  }
}
