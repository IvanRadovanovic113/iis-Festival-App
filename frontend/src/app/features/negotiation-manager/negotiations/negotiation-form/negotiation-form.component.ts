import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NegotiationService } from '../../../../core/services/negotiation.service';
import { OfferService } from '../../../../core/services/offer.service';
import { PerformerService } from '../../../../core/services/performer.service';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';

@Component({
  selector: 'app-negotiation-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './negotiation-form.component.html',
  styleUrls: ['./negotiation-form.component.css']
})
export class NegotiationFormComponent implements OnInit {
  offer: any;
  performer: any;
  template: any;
  notes: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private negotiationService: NegotiationService,
    private offerService: OfferService,
    private performerService: PerformerService,
    private workflowService: WorkflowTemplateService
  ) {}

  ngOnInit(): void {
    const offerId = this.route.snapshot.queryParamMap.get('offerId');
    const performerId = this.route.snapshot.queryParamMap.get('performerId');

    if (!offerId || !performerId) {
      console.error("Nedostaju parametri u URL-u!");
      this.router.navigate(['/negotiation-manager/negotiations']);
      return;
    }

    this.loadData(Number(offerId), Number(performerId));
  }

  loadData(offerId: number, performerId: number): void {
    this.offerService.getOfferById(offerId).subscribe(o => {
      this.offer = o;
      this.workflowService.getTemplateById(o.workflowTemplateId).subscribe(t => this.template = t);
    });

    this.performerService.getPerformerById(performerId).subscribe(p => this.performer = p);
  }

  confirmAndStart(): void {
    const offerId = this.offer.offerId;
    const performerId = this.performer.performerId;
    
    this.negotiationService.startNegotiation(offerId, performerId).subscribe({
        next: () => {
        console.log('Negotiation started successfully');
        this.router.navigate(['/negotiation-manager/negotiations']);
        },
        error: (err) => {
        console.error('Error starting negotiation', err);
        alert('Failed to start negotiation.');
        }
    });
  }
}