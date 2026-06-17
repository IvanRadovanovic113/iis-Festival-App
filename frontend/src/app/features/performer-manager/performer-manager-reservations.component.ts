import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { PerformerManagerReservationService } from '../../core/services/performer-manager-reservation.service';
import {
  ContractReservationCustomResourceRequest,
  PerformerContractReservation
} from '../../core/models/performer-manager.model';

@Component({
  selector: 'app-performer-manager-reservations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './performer-manager-reservations.component.html',
  styleUrls: ['./performer-manager-reservations.component.css']
})
export class PerformerManagerReservationsComponent implements OnInit {
  private readonly reservationService = inject(PerformerManagerReservationService);
  private readonly authService = inject(AuthService);

  contracts: PerformerContractReservation[] = [];
  selectedContract: PerformerContractReservation | null = null;
  selectedResources: Record<number, boolean> = {};
  resourceQuantities: Record<number, number> = {};
  customResources: ContractReservationCustomResourceRequest[] = [];
  notes = '';
  loading = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.loadContracts();
  }

  get pendingContracts(): PerformerContractReservation[] {
    return this.contracts.filter(contract => !contract.reservationRequestId);
  }

  get submittedContracts(): PerformerContractReservation[] {
    return this.contracts.filter(contract => contract.reservationRequestId);
  }

  loadContracts(selectedContractId?: number): void {
    this.loading = true;
    this.clearMessages();
    this.reservationService.getContracts().subscribe({
      next: contracts => {
        this.contracts = contracts;
        const nextSelected = selectedContractId
          ? contracts.find(contract => contract.contractId === selectedContractId)
          : contracts.find(contract => !contract.reservationRequestId) ?? contracts[0];
        this.selectContract(nextSelected ?? null);
        this.loading = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Unable to load confirmed contracts.';
        this.loading = false;
      }
    });
  }

  selectContract(contract: PerformerContractReservation | null): void {
    this.selectedContract = contract;
    this.selectedResources = {};
    this.resourceQuantities = {};
    this.customResources = [];
    this.notes = '';
    this.clearMessages();

    contract?.stageResources.forEach(resource => {
      this.resourceQuantities[resource.resourceId] = 1;
    });
  }

  toggleResource(resourceId: number, checked: boolean): void {
    this.selectedResources[resourceId] = checked;
  }

  addCustomResource(): void {
    this.customResources = [
      ...this.customResources,
      { requestedName: '', requestedType: 'Equipment', quantity: 1 }
    ];
  }

  removeCustomResource(index: number): void {
    this.customResources = this.customResources.filter((_, itemIndex) => itemIndex !== index);
  }

  createReservationRequest(): void {
    if (!this.selectedContract || this.selectedContract.reservationRequestId) return;

    const existingResources = this.selectedContract.stageResources
      .filter(resource => this.selectedResources[resource.resourceId])
      .map(resource => ({
        resourceId: resource.resourceId,
        quantity: Number(this.resourceQuantities[resource.resourceId] || 1)
      }));

    const customResources = this.customResources
      .map(resource => ({
        requestedName: resource.requestedName.trim(),
        requestedType: resource.requestedType.trim(),
        quantity: Number(resource.quantity || 1)
      }))
      .filter(resource => resource.requestedName && resource.requestedType);

    if (!this.validateExistingResources(existingResources)) return;
    if (!this.validateCustomResources(customResources)) return;

    this.submitting = true;
    this.clearMessages();
    this.reservationService.createReservationRequest(this.selectedContract.contractId, {
      existingResources,
      customResources,
      notes: this.notes.trim() || null
    }).subscribe({
      next: () => {
        this.successMessage = 'Reservation request sent to event organization.';
        this.submitting = false;
        this.loadContracts(this.selectedContract!.contractId);
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Unable to create reservation request.';
        this.submitting = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  private validateExistingResources(resources: { resourceId: number; quantity: number }[]): boolean {
    if (!this.selectedContract) return false;
    for (const resource of resources) {
      const stageResource = this.selectedContract.stageResources.find(item => item.resourceId === resource.resourceId);
      if (!stageResource || resource.quantity < 1 || resource.quantity > stageResource.quantity) {
        this.errorMessage = 'Requested stage resource quantities must fit the available stage inventory.';
        return false;
      }
    }
    return true;
  }

  private validateCustomResources(resources: ContractReservationCustomResourceRequest[]): boolean {
    if (resources.length !== this.customResources.length && this.customResources.some(resource => resource.requestedName.trim() || resource.requestedType.trim())) {
      this.errorMessage = 'Custom resources need a name, type, and quantity.';
      return false;
    }
    if (resources.some(resource => resource.quantity < 1)) {
      this.errorMessage = 'Custom resource quantities must be positive.';
      return false;
    }
    return true;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
