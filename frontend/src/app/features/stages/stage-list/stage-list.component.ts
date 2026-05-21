import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BinaService } from '../../../core/services/bina.service';
import { AuthService } from '../../../core/services/auth.service';
import { Bina, BinaRequest } from '../../../core/models/bina.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-stage-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './stage-list.component.html',
  styleUrls: ['./stage-list.component.css']
})
export class StageListComponent implements OnInit {
  private binaService = inject(BinaService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  bine: Bina[] = [];
  currentUser: User | null = null;
  errorMessage = '';

  formMode: 'create' | 'edit' | null = null;
  selectedBina: Bina | null = null;

  form = this.fb.group({
    naziv: ['', [Validators.required, Validators.minLength(2)]],
    kapacitet: [null as number | null, [Validators.required, Validators.min(1)]],
    lokacija: ['', [Validators.required, Validators.minLength(2)]]
  });

  get naziv() { return this.form.get('naziv')!; }
  get kapacitet() { return this.form.get('kapacitet')!; }
  get lokacija() { return this.form.get('lokacija')!; }

  get festivalNaziv(): string {
    return this.currentUser?.assignment?.festivalNaziv ?? '';
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => this.currentUser = user);
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.binaService.getAll().subscribe({
      next: data => this.bine = data,
      error: () => this.errorMessage = 'Greška pri učitavanju bina.'
    });
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedBina = null;
    this.form.reset();
  }

  openEdit(bina: Bina): void {
    this.formMode = 'edit';
    this.selectedBina = bina;
    this.form.patchValue({
      naziv: bina.naziv,
      kapacitet: bina.kapacitet,
      lokacija: bina.lokacija
    });
  }

  cancelForm(): void {
    this.formMode = null;
    this.selectedBina = null;
    this.form.reset();
    this.errorMessage = '';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.value as BinaRequest;
    const op = this.formMode === 'edit' && this.selectedBina
      ? this.binaService.update(this.selectedBina.binaId, request)
      : this.binaService.create(request);

    op.subscribe({
      next: () => {
        this.cancelForm();
        this.load();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Greška pri čuvanju bine.';
      }
    });
  }

  delete(bina: Bina): void {
    if (!confirm(`Da li ste sigurni da želite da obrišete binu "${bina.naziv}"?`)) return;
    this.binaService.delete(bina.binaId).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage = 'Greška pri brisanju bine.'
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
