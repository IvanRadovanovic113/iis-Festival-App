import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TierConfigService } from '../../../core/services/tier-config.service';
import { TierConfig } from '../../../core/models/tier-config.model';

interface TierRow extends TierConfig {
  saving: boolean;
  saved: boolean;
  error: string;
}

@Component({
  selector: 'app-tier-config',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './tier-config.component.html',
  styleUrls: ['./tier-config.component.css']
})
export class TierConfigComponent implements OnInit {
  private tierConfigService = inject(TierConfigService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  rows: TierRow[] = [];
  loading = true;
  loadError = '';

  ngOnInit(): void {
    this.tierConfigService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: configs => {
          this.rows = configs.map(c => ({ ...c, saving: false, saved: false, error: '' }));
          this.loading = false;
        },
        error: () => {
          this.loadError = 'Failed to load tier configuration.';
          this.loading = false;
        }
      });
  }

  save(row: TierRow): void {
    if (row.saving) return;
    row.saving = true;
    row.saved = false;
    row.error = '';

    this.tierConfigService.update(row.tier, row.minTickets, row.discountPercent)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          row.minTickets = updated.minTickets;
          row.discountPercent = updated.discountPercent;
          row.saving = false;
          row.saved = true;
          setTimeout(() => row.saved = false, 3000);
        },
        error: err => {
          row.error = err.error?.message || 'Save failed.';
          row.saving = false;
        }
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  tierLabel(tier: string): string {
    const labels: Record<string, string> = { BRONZE: 'Bronze', SILVER: 'Silver', GOLD: 'Gold' };
    return labels[tier] ?? tier;
  }
}
