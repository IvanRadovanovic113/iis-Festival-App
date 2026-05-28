import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OfferService } from '../../../../core/services/offer.service';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';
import { OfferRequest, OfferStatus } from '../../../../core/models/offer.model';
import { WorkflowTemplateResponse } from '../../../../core/models/workflow.model';

@Component({
  selector: 'app-offer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './offer-form.component.html',
  styleUrls: ['./offer-form.component.css']
})
export class OfferFormComponent implements OnInit {
  offerForm!: FormGroup;
  isEditMode = false;
  offerId?: number;

  templates: WorkflowTemplateResponse[] = [];
  selectedTemplateSteps: string[] = [];
  selectedTemplateDetail = '';

  constructor(
    private fb: FormBuilder,
    private offerService: OfferService,
    private workflowService: WorkflowTemplateService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadTemplates();
    this.offerId = Number(this.route.snapshot.paramMap.get('offerId'));
    if (this.offerId) {
      this.isEditMode = true;
      this.loadOfferForEdit();
    }
  }

  loadTemplates(): void {
    this.workflowService.getAllTemplates().subscribe({
      next: (data) => {
        this.templates = data;
      },
      error: (err) => console.error('Failed to load templates', err)
    });
  }

  initForm(): void {
    this.offerForm = this.fb.group({
      location: ['', [Validators.required, Validators.maxLength(255)]],
      dateOnly: ['', Validators.required],
      timeOnly: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(1)]],
      durationMinutes: [90, [Validators.required, Validators.min(1)]],
      additionalRequirements: ['', Validators.maxLength(2000)],
      workflowTemplateId: ['', Validators.required]
    });

    // Povezivanje pravih podataka kada se izabere šablon
    this.offerForm.get('workflowTemplateId')?.valueChanges.subscribe(id => {
      if (!id) {
        this.selectedTemplateSteps = [];
        this.selectedTemplateDetail = '';
        return;
      }
      
      // Pozivamo detalje šablona sa servera
      this.workflowService.getTemplateById(+id).subscribe({
        next: (detail) => {
          // Uzimamo imena stanja iz detaljnog odgovora
          this.selectedTemplateSteps = detail.states.map(s => s.name);
          this.selectedTemplateDetail = `${detail.states.length} states · ${detail.transitions.length} conditions total`;
        }
      });
    });
  }

  loadOfferForEdit(): void {
    this.offerService.getOfferById(this.offerId!).subscribe({
      next: (data) => {
        if (data.status !== OfferStatus.DRAFT) {
          this.router.navigate(['/negotiation-manager/offers']);
          return;
        }
        const dt = new Date(data.performanceDate);
        this.offerForm.patchValue({
          location: data.location,
          dateOnly: dt.toISOString().split('T')[0],
          timeOnly: dt.toTimeString().split(' ')[0].substring(0, 5),
          price: data.price,
          durationMinutes: data.durationMinutes,
          additionalRequirements: data.additionalRequirements,
          workflowTemplateId: data.workflowTemplateId
        });
      }
    });
  }

  private buildRequest(): OfferRequest {
    const raw = this.offerForm.value;
    return {
      location: raw.location,
      performanceDate: `${raw.dateOnly}T${raw.timeOnly}:00`,
      price: raw.price,
      durationMinutes: raw.durationMinutes,
      additionalRequirements: raw.additionalRequirements,
      workflowTemplateId: +raw.workflowTemplateId
    };
  }

  onSubmit(saveAndPublish: boolean): void {
    if (this.offerForm.invalid) return;
    const requestData = this.buildRequest();

    if (this.isEditMode) {
      this.offerService.updateOffer(this.offerId!, requestData).subscribe({
        next: () => this.router.navigate(['/negotiation-manager/offers'])
      });
    } else {
      this.offerService.createOffer(requestData).subscribe({
        next: (res) => {
            if (saveAndPublish) {
                this.offerService.publishOffer(res.offerId).subscribe(() => this.router.navigate(['/negotiation-manager/offers']));
            } else {
                this.router.navigate(['/negotiation-manager/offers']);
            }
        }
      });
    }
  }
}