import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NegotiationService } from '../../../../core/services/negotiation.service';
import { NegotiationResponse } from '../../../../core/models/negotiation.model';

@Component({
  selector: 'app-negotiation-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './negotiation-list.component.html',
  styleUrls: ['./negotiation-list.component.css']
})
export class NegotiationListComponent implements OnInit {
  negotiations: NegotiationResponse[] = [];
  searchTerm: string = '';
  selectedStatus: string = '';
  
  statuses = ['ACTIVE', 'COMPLETED', 'FAILED'];

  constructor(private negotiationService: NegotiationService) {}

  ngOnInit(): void {
    this.loadNegotiations();
  }

  loadNegotiations(): void {
    this.negotiationService.getAllNegotiations().subscribe({
      next: (data: any) => {
        const negotiationsArray = data.content ? data.content : data;

        this.negotiations = negotiationsArray.filter((n: any) => {
          const matchesSearch = n.performerName?.toLowerCase().includes(this.searchTerm.toLowerCase()) || 
                                n.offerTitle?.toLowerCase().includes(this.searchTerm.toLowerCase());
          const matchesStatus = !this.selectedStatus || n.status === this.selectedStatus;
          return matchesSearch && matchesStatus;
        });
      },
      error: (err) => console.error('Error loading negotiations:', err)
    });
  }

  onFilterChange(): void {
    this.loadNegotiations();
  }
}