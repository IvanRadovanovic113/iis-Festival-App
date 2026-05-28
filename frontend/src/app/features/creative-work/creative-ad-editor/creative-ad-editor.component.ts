import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly campaignService = inject(CampaignService);

  ad: Ad | null = null;
  errorMessage = '';
  saving = false;
  selectedFileName = '';
  previewUrl = '';
  originalContentValue = '';
  contentCleared = false;
  readonly currentUser = this.authService.getCurrentUser();
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

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

  get displayName(): string {
    return this.currentUser?.username || 'User';
  }

  get avatarLabel(): string {
    const name = this.displayName.trim();
    const parts = name.split(/[._-]+/).filter(Boolean);
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
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

  get hasPreview(): boolean {
    return !!this.previewUrl;
  }

  get hasExistingUploadedContent(): boolean {
    return !!this.originalContentValue;
  }

  get isImagePreview(): boolean {
    return this.previewUrl.startsWith('data:image/');
  }

  get isAudioPreview(): boolean {
    return this.previewUrl.startsWith('data:audio/');
  }

  get isVideoPreview(): boolean {
    return this.previewUrl.startsWith('data:video/');
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
        this.originalContentValue = ad.contentValue ?? '';
        this.contentCleared = false;
        this.selectedFileName = this.resolveDisplayFileName(ad.contentValue ?? '');
        this.previewUrl = this.resolvePreviewUrl(ad.contentValue ?? '');
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
      this.clearSelectedContent();
      return;
    }

    if (!this.isAcceptedFileType(file)) {
      this.errorMessage = `Selected file does not match required content type: ${this.ad?.contentType}.`;
      input.value = '';
      this.clearSelectedContent();
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.errorMessage = '';
      this.contentCleared = false;
      this.selectedFileName = file.name;
      this.previewUrl = typeof reader.result === 'string' ? reader.result : '';
      this.form.patchValue({ contentValue: this.previewUrl });
    };
    reader.onerror = () => {
      this.errorMessage = 'Error reading selected file.';
      this.clearSelectedContent();
    };
    reader.readAsDataURL(file);
  }

  private isAcceptedFileType(file: File): boolean {
    if (!this.fileAccept) return false;
    const majorType = this.fileAccept.replace('/*', '');
    return file.type.startsWith(`${majorType}/`);
  }

  private resolvePreviewUrl(contentValue: string): string {
    return contentValue.startsWith('data:') ? contentValue : '';
  }

  private resolveDisplayFileName(contentValue: string): string {
    if (!contentValue) {
      return '';
    }
    if (contentValue.startsWith('data:video/')) {
      return 'Current uploaded video';
    }
    if (contentValue.startsWith('data:audio/')) {
      return 'Current uploaded audio';
    }
    if (contentValue.startsWith('data:image/')) {
      return 'Current uploaded image';
    }
    return contentValue;
  }

  clearSelectedContent(): void {
    this.selectedFileName = '';
    this.previewUrl = '';
    this.contentCleared = true;
    this.form.patchValue({ contentValue: '' });
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
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
        this.originalContentValue = this.ad.contentValue ?? '';
        this.contentCleared = false;
        this.selectedFileName = this.resolveDisplayFileName(this.ad.contentValue ?? '');
        this.previewUrl = this.resolvePreviewUrl(this.ad.contentValue ?? '');
        this.form.patchValue({
          contentValue: this.ad.contentValue ?? ''
        });
        this.saving = false;
        void this.router.navigate(['/creative/campaigns', this.route.snapshot.paramMap.get('campaignId')], {
          queryParams: { saved: '1' }
        });
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
