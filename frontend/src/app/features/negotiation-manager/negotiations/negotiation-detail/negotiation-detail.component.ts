import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NegotiationService } from '../../../../core/services/negotiation.service';
import { NegotiationDetailsResponse, ConditionValueDto } from '../../../../core/models/negotiation.model';

@Component({
  selector: 'app-negotiation-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './negotiation-detail.component.html',
  styleUrls: ['./negotiation-detail.component.css']
})
export class NegotiationDetailComponent implements OnInit {
  negotiation!: NegotiationDetailsResponse;
  conditionInputs: { [key: number]: string | boolean | number } = {};
  selectedTransition: any = null;

  constructor(
    private route: ActivatedRoute,
    private negotiationService: NegotiationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadDetails(id);
  }

  loadDetails(id: number): void {
    this.negotiationService.getNegotiationDetails(id).subscribe({
      next: (data) => {
        this.negotiation = {
          ...data,
          history: data.history.map(h => ({
            ...h,
            duration: h.exitTime 
              ? this.calculateDuration(h.entryTime, h.exitTime) 
              : 'Active' 
          }))
        };

        data.enteredConditions.forEach(c => this.conditionInputs[c.conditionId] = c.value);
      }
    });
  }

  getConditionLabel(id: number): string {
    for (const trans of this.negotiation.availableTransitions) {
      const found = trans.requiredConditions.find((c: any) => c.id === id);
      if (found) return found.label;
    }
    return 'Condition #' + id;
  }

  selectTransition(transition: any): void {
    this.selectedTransition = transition;

      transition.requiredConditions.forEach((cond: any) => {
      if (this.conditionInputs[cond.id] === undefined) {
        this.conditionInputs[cond.id] = cond.dataType === 'BOOLEAN' ? false : '';
      }
    });
  }

  async onTransition(): Promise<void> {
    if (!this.selectedTransition) return;

    // 1. Pripremi uslove
    const conditions: ConditionValueDto[] = Object.entries(this.conditionInputs).map(([id, val]) => ({
      conditionId: Number(id),
      value: String(val)
    }));

    try {
      await this.negotiationService.saveNegotiationConditions(this.negotiation.id, conditions).toPromise();

      await this.negotiationService.performTransition(this.negotiation.id, this.selectedTransition.id).toPromise();

      this.selectedTransition = null;
      this.loadDetails(this.negotiation.id);
    } catch (err: any) {
      alert('Operacija nije uspela: ' + (err.error?.message || err.message));
    }
  }

  private calculateDuration(start: string | Date, end: string | Date): string {
    const diffMs = new Date(end).getTime() - new Date(start).getTime();
    const diffMins = Math.floor(diffMs / 60000);
    return diffMins > 0 ? `${diffMins} min` : '< 1 min';
  }

  async completeNegotiation(): Promise<void> {
    if (!confirm('Da li ste sigurni da želite da zaključite pregovor?')) return;

    this.negotiationService.completeNegotiation(this.negotiation.id).subscribe({
      next: () => {
        this.loadDetails(this.negotiation.id);
      },
      error: (err) => alert('Greška pri zaključivanju: ' + err.message)
    });
  }

  async failNegotiation(): Promise<void> {
    const reason = prompt("Unesite razlog za neuspeh pregovora:");
    
    if (reason === null) return;
    if (reason.trim() === '') {
      alert("Razlog je obavezan!");
      return;
    }

    this.negotiationService.failNegotiation(this.negotiation.id, reason).subscribe({
      next: () => {
        alert('Pregovor je prekinut.');
        this.loadDetails(this.negotiation.id);
      },
      error: (err) => alert('Greška: ' + (err.error?.message || err.message))
    });
  }

  isFinalState(): boolean {
    return this.negotiation && 
          this.negotiation.availableTransitions.length === 0 && 
          this.negotiation.status === 'ACTIVE';
  }
}