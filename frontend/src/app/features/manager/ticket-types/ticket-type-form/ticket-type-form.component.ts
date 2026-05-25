import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { TicketTypeService } from '../../../../core/services/ticket-type.service';
import { SegmentService } from '../../../../core/services/segment.service';
import { TicketTypeRequest } from '../../../../core/models/ticket-type.model';
import { Segment } from '../../../../core/models/segment.model';
import { User } from '../../../../core/models/user.model';

@Component({
  selector: 'app-ticket-type-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ticket-type-form.component.html',
  styleUrls: ['./ticket-type-form.component.css']
})
export class TicketTypeFormComponent implements OnInit {
  private authService = inject(AuthService);
  private ticketTypeService = inject(TicketTypeService);
  private segmentService = inject(SegmentService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  currentUser: User | null = null;
  segments: Segment[] = [];
  selectedSegmentIds = new Set<number>();
  editId: number | null = null;
  errorMessage = '';
  submitting = false;

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    totalQuantity: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  get isEdit(): boolean { return this.editId !== null; }
  get name() { return this.form.get('name')!; }
  get totalQuantity() { return this.form.get('totalQuantity')!; }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) this.editId = +idParam;

    this.authService.currentUser.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user => {
      this.currentUser = user;
      if (user?.assignment?.festivalId) {
        this.loadSegments(user.assignment.festivalId);
      }
    });

    if (this.editId) {
      this.ticketTypeService.getById(this.editId).subscribe({
        next: tt => {
          this.form.patchValue({ name: tt.name, totalQuantity: tt.totalQuantity });
          this.selectedSegmentIds = new Set(tt.segments.map(s => s.segmentId));
        },
        error: () => this.errorMessage = 'Error loading ticket type.'
      });
    }
  }

  private loadSegments(festivalId: number): void {
    this.segmentService.getFestivalSegments(festivalId).subscribe({
      next: data => this.segments = data,
      error: () => this.errorMessage = 'Error loading segments.'
    });
  }

  toggleSegment(segmentId: number): void {
    if (this.selectedSegmentIds.has(segmentId)) {
      this.selectedSegmentIds.delete(segmentId);
    } else {
      this.selectedSegmentIds.add(segmentId);
    }
  }

  isSelected(segmentId: number): boolean {
    return this.selectedSegmentIds.has(segmentId);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.errorMessage = '';

    const request: TicketTypeRequest = {
      name: this.form.value.name!,
      totalQuantity: this.form.value.totalQuantity!,
      segmentIds: Array.from(this.selectedSegmentIds)
    };

    const op = this.isEdit
      ? this.ticketTypeService.update(this.editId!, request)
      : this.ticketTypeService.create(this.currentUser!.assignment!.festivalId, request);

    op.subscribe({
      next: () => this.router.navigate(['../..'], { relativeTo: this.route }),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error saving ticket type.';
        this.submitting = false;
      }
    });
  }
}
