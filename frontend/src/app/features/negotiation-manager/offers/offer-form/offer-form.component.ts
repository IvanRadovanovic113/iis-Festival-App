import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OfferService } from '../../../../core/services/offer.service';
import { OfferRequest, OfferStatus } from '../../../../core/models/offer.model';

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

  // Hardkodovana lista prema zahtevu (Slika 2)
  templates = [
    { id: 1, name: 'Standard Band Negotiation', steps: ['Initial Contact', 'Terms Discussion', 'Contract Review', 'Final Agreement'], detail: '4 states · 12 conditions total' },
    { id: 2, name: 'Main Stage Solo Track', steps: ['Initial Contact', 'Contract Review', 'Final Agreement'], detail: '3 states · 8 conditions total' }
  ];
  selectedTemplateSteps: string[] = [];
  selectedTemplateDetail = '';

  constructor(
    private fb: FormBuilder,
    private offerService: OfferService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.offerId = Number(this.route.snapshot.paramMap.get('offerId'));
    if (this.offerId) {
      this.isEditMode = true;
      this.loadOfferForEdit();
    }
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

    // Praćenje promene šablona radi crtanja preview-a (Slika 2)
    this.offerForm.get('workflowTemplateId')?.valueChanges.subscribe(id => {
      const found = this.templates.find(t => t.id === +id);
      this.selectedTemplateSteps = found ? found.steps : [];
      this.selectedTemplateDetail = found ? found.detail : '';
    });
  }

  loadOfferForEdit(): void {
    this.offerService.getOfferById(this.offerId!).subscribe({
      next: (data) => {
        if (data.status !== OfferStatus.DRAFT) {
          alert('Only DRAFT offers can be edited!');
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
    const combinedDateTime = `${raw.dateOnly}T${raw.timeOnly}:00`;
    return {
      location: raw.location,
      performanceDate: combinedDateTime,
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
        next: (res) => this.handleSuccess(res.offerId, saveAndPublish)
      });
    } else {
      this.offerService.createOffer(requestData).subscribe({
        next: (res) => this.handleSuccess(res.offerId, saveAndPublish)
      });
    }
  }

  handleSuccess(id: number, publish: boolean): void {
    if (publish) {
      this.offerService.publishOffer(id).subscribe({
        next: () => this.router.navigate(['/negotiation-manager/offers'])
      });
    } else {
      this.router.navigate(['/negotiation-manager/offers']);
    }
  }
}