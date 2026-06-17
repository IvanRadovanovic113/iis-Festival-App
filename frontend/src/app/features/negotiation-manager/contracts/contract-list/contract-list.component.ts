import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ContractService} from '../../../../core/services/contract.service';
import { Contract } from '../../../../core/models/contract.model';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-contract-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './contract-list.component.html',
  styleUrls: ['./contract-list.component.css']
})
export class ContractListComponent implements OnInit {
  contracts: Contract[] = [];
  filteredContracts: Contract[] = [];
  searchTerm: string = '';

  constructor(private contractService: ContractService) {}

  ngOnInit(): void {
    this.contractService.getAll().subscribe({
      next: (data) => {
        this.contracts = data;
        this.filteredContracts = data;
      },
      error: (err) => console.error('Error loading contracts:', err)
    });
  }

  onSearch(): void {
    this.filteredContracts = this.contracts.filter(c => 
      c.offerTitle.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      c.performerName.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }
}