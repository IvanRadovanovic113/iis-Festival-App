import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { PerformerService } from '../../../../core/services/performer.service';
import { PerformerResponse, PerformerStatus, PerformerType } from '../../../../core/models/performer.model';
import { debounceTime, distinctUntilChanged, switchMap, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-performer-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './performer-list.component.html',
  styleUrls: ['./performer-list.component.css']
})
export class PerformerListComponent implements OnInit {
  private performerService = inject(PerformerService);
  private destroyRef = inject(DestroyRef);

  performers: PerformerResponse[] = [];
  
  // Parametri za paginaciju
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Enumi dostupni u HTML-u
  PerformerStatus = PerformerStatus;
  PerformerType = PerformerType;

  // Reaktivna forma za filtere sa wireframe-a
  filterForm = new FormGroup({
    status: new FormControl<PerformerStatus>(PerformerStatus.ACTIVE),
    searchName: new FormControl(''),
    genre: new FormControl(''),
    performerType: new FormControl(''),
    countryOfOrigin: new FormControl(''),
    numberOfMembers: new FormControl<number | null>(null)
  });

  ngOnInit(): void {
    this.loadPerformers();

    // Automatsko osvežavanje tabele kada korisnik promeni filtere
    this.filterForm.valueChanges.pipe(
      debounceTime(300), // Čeka 300ms nakon kucanja
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.currentPage = 0; // Resetuje na prvu stranu pri novom filtriranju
      this.loadPerformers();
    });
  }

  loadPerformers(): void {
    const filters = this.filterForm.value;
    this.performerService.getPerformers(filters, this.currentPage, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.performers = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
        },
        error: (err) => {
          console.error('Error loading performers:', err);
        }
      });
  }

  // Kontrola strana
  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadPerformers();
    }
  }

  // Brza promena tabova (Active vs Archived) prateći stil tvojih Offers
  setStatusFilter(status: PerformerStatus): void {
    this.filterForm.patchValue({ status });
  }
}