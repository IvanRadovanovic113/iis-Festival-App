import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PerformerService } from '../../../../core/services/performer.service';
import { PerformerResponse, PerformerStatus } from '../../../../core/models/performer.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-performer-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './performer-detail.component.html',
  styleUrls: ['./performer-detail.component.css']
})
export class PerformerDetailComponent implements OnInit {
  private performerService = inject(PerformerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  performer: PerformerResponse | null = null;
  errorMessage: string | null = null;
  PerformerStatus = PerformerStatus;

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.loadPerformer(+id);
        }
      });
  }

  loadPerformer(id: number): void {
    this.performerService.getPerformerById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data: PerformerResponse) => this.performer = data,
        error: (err: any) => {
          this.errorMessage = 'Could not load performer details. It may have been deleted.';
          console.error(err);
        }
      });
  }

  toggleStatus(): void {
    if (!this.performer) return;

    // Pošto tvoj servis trenutno ima samo archivePerformer metodu,
    // pozivamo nju ukoliko je performer trenutno ACTIVE.
    if (this.performer.status === PerformerStatus.ACTIVE) {
      this.performerService.archivePerformer(this.performer.performerId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (updatedPerformer: PerformerResponse) => {
            this.performer = updatedPerformer;
          },
          error: (err: any) => {
            this.errorMessage = 'Failed to archive performer.';
            console.error(err);
          }
        });
    } else {
      this.errorMessage = 'Activation endpoint is not implemented in the service yet.';
    }
  }
}