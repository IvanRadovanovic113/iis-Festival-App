import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ContractService } from '../../../../core/services/contract.service';
import { Contract } from '../../../../core/models/contract.model';

@Component({
  selector: 'app-contract-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './contract-detail.component.html',
  styleUrls: ['./contract-detail.component.css']
})
export class ContractDetailComponent implements OnInit {
  contract: Contract | null = null;

  constructor(
    private route: ActivatedRoute,
    private contractService: ContractService
  ) {}

ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.contractService.getById(id).subscribe({
        next: (data) => {
            const processedContract = { ...data };
            
            if (processedContract.conditionSnapshotJson) {
                try {
                    processedContract.snapshotData = JSON.parse(processedContract.conditionSnapshotJson);
                } catch (e) {
                    processedContract.snapshotData = processedContract.conditionSnapshotJson;
                }
            }
            this.contract = processedContract;
        },
        error: (err) => {
            console.error("GRESKA U SERVISU:", err);
        }
    });
}
}