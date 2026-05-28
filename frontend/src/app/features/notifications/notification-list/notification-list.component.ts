import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';
import { AuthService } from '../../../core/services/auth.service';
import { WorkflowNotification } from '../../../core/models/campaign.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.css']
})
export class NotificationListComponent implements OnInit {
  private readonly campaignService = inject(CampaignService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  notifications: WorkflowNotification[] = [];
  errorMessage = '';
  readonly currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    this.load();
  }

  get role(): string {
    return this.currentUser?.assignment?.festivalRole ?? '';
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

  get primaryLabel(): string {
    return this.isCreativeRole ? 'Campaigns' : 'Festivals';
  }

  get primaryLink(): string {
    return this.isCreativeRole ? '/creative/campaigns' : this.role === 'FESTIVAL_DIRECTOR' ? '/director/festivals' : '/manager/festivals';
  }

  get statisticsLink(): string {
    return this.role === 'FESTIVAL_DIRECTOR' ? '/director/statistics' : '/manager/statistics';
  }

  get isCreativeRole(): boolean {
    return this.role === 'PRODUCT_DESIGNER' || this.role === 'TECHNICAL_SUPPORT';
  }

  load(): void {
    this.errorMessage = '';
    this.campaignService.getNotifications().subscribe({
      next: notifications => this.notifications = notifications,
      error: () => this.errorMessage = 'Error loading notifications.'
    });
  }

  openNotification(notification: WorkflowNotification): void {
    const navigate = () => this.router.navigateByUrl(this.getNotificationLink(notification));
    if (notification.read) {
      navigate();
      return;
    }
    this.campaignService.markNotificationAsRead(notification.notificationId).subscribe({
      next: updated => {
        this.notifications = this.notifications.map(item => item.notificationId === updated.notificationId ? updated : item);
        navigate();
      },
      error: () => navigate()
    });
  }

  markAsRead(notification: WorkflowNotification): void {
    if (notification.read) {
      return;
    }
    this.campaignService.markNotificationAsRead(notification.notificationId).subscribe({
      next: updated => {
        this.notifications = this.notifications.map(item => item.notificationId === updated.notificationId ? updated : item);
      }
    });
  }

  getNotificationLink(notification: WorkflowNotification): string {
    if (this.role === 'FESTIVAL_DIRECTOR') {
      if (notification.type === 'PROMOTION_ENDING_SOON') {
        return `/director/festivals/${notification.festivalId}/campaign/ads/${notification.adId}?publish=1`;
      }
      return `/director/festivals/${notification.festivalId}/campaign/ads/${notification.adId}`;
    }
    if (this.role === 'FESTIVAL_MANAGER') {
      return `/manager/festivals/${notification.festivalId}/campaign/ads/${notification.adId}`;
    }
    return `/creative/campaigns/${notification.campaignId}/ads/${notification.adId}`;
  }

  getActionLabel(notification: WorkflowNotification): string {
    return notification.type === 'PROMOTION_ENDING_SOON' ? 'Prolong' : 'Open';
  }

  logout(): void {
    this.authService.logout();
  }
}
