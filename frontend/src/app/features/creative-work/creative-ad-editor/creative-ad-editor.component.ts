import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CampaignService } from '../../../core/services/campaign.service';
import { Ad } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-creative-ad-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './creative-ad-editor.component.html',
  styleUrls: ['./creative-ad-editor.component.css']
})
export class CreativeAdEditorComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  ad: Ad | null = null;
  errorMessage = '';
  saving = false;
  selectedFileName = '';
  readonly currentUser = this.authService.getCurrentUser();

  form = this.fb.group({
    contentValue: ['', Validators.required]
  });

  ngOnInit(): void {
    this.load();
  }

  get roleLabel(): string {
    const role = this.currentUser?.assignment?.festivalRole;
    return role === 'PRODUCT_DESIGNER' ? 'Product designer' : 'Technical support';
  }

  get expectsTextContent(): boolean {
    return this.ad?.contentType === 'Text';
  }

  get fileAccept(): string {
    switch (this.ad?.contentType) {
      case 'Image':
        return 'image/*';
      case 'Audio':
        return 'audio/*';
      case 'Video':
        return 'video/*';
      default:
        return '';
    }
  }

  get usesFileUpload(): boolean {
    return !!this.fileAccept;
  }

  get contentLabel(): string {
    switch (this.ad?.contentType) {
      case 'Text':
        return 'Text content';
      case 'Image':
        return 'Image file';
      case 'Audio':
        return 'Audio file';
      case 'Video':
        return 'Video file';
      default:
        return 'Content';
    }
  }

  get uploadHint(): string {
    switch (this.ad?.contentType) {
      case 'Image':
        return 'Only image files are allowed.';
      case 'Audio':
        return 'Only audio files are allowed.';
      case 'Video':
        return 'Only video files are allowed.';
      case 'Text':
        return 'Enter the final text content for this ad.';
      case 'Interactive':
        return 'Enter a reference, URL, or implementation note for this interactive asset.';
      default:
        return 'Enter the final content value for this ad.';
    }
  }

  load(): void {
    const campaignId = Number(this.route.snapshot.paramMap.get('campaignId'));
    const adId = Number(this.route.snapshot.paramMap.get('adId'));
    this.errorMessage = '';
    this.campaignService.getCreativeAd(campaignId, adId).subscribe({
      next: ad => {
        this.ad = ad;
        this.selectedFileName = ad.contentValue ?? '';
        this.form.patchValue({
          contentValue: ad.contentValue ?? ''
        });
      },
      error: () => this.errorMessage = 'Error loading ad.'
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      this.selectedFileName = '';
      this.form.patchValue({ contentValue: '' });
      return;
    }

    if (!this.isAcceptedFileType(file)) {
      this.errorMessage = `Selected file does not match required content type: ${this.ad?.contentType}.`;
      input.value = '';
      this.selectedFileName = '';
      this.form.patchValue({ contentValue: '' });
      return;
    }

    this.errorMessage = '';
    this.selectedFileName = file.name;
    this.form.patchValue({ contentValue: file.name });
  }

  private isAcceptedFileType(file: File): boolean {
    if (!this.fileAccept) return false;
    const majorType = this.fileAccept.replace('/*', '');
    return file.type.startsWith(`${majorType}/`);
  }

  save(): void {
    if (!this.ad || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.campaignService.updateCreativeAd(Number(this.route.snapshot.paramMap.get('campaignId')), this.ad.adId, this.form.getRawValue() as {
      contentValue: string;
    }).subscribe({
      next: updatedAd => {
        this.ad = updatedAd;
        this.selectedFileName = this.ad.contentValue ?? '';
        this.form.patchValue({
          contentValue: this.ad.contentValue ?? ''
        });
        this.saving = false;
      },
      error: err => {
        this.errorMessage = err?.error?.message ?? 'Error saving ad.';
        this.saving = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
