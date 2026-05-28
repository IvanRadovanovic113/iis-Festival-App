import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { NegotiationManagerLayoutComponent } from './features/negotiation-manager/negotiation-manager-layout/negotiation-manager-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'pending',
    loadComponent: () =>
      import('./features/pending/pending.component').then(m => m.PendingComponent),
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard],
    data: { adminOnly: true }
  },
  {
    path: 'admin/festivals',
    loadComponent: () =>
      import('./features/festivals/festival-list/festival-list.component').then(m => m.FestivalListComponent),
    canActivate: [authGuard],
    data: { adminOnly: true }
  },
  {
    path: 'admin/festivals/new',
    loadComponent: () =>
      import('./features/festivals/festival-form/festival-form.component').then(m => m.FestivalFormComponent),
    canActivate: [authGuard],
    data: { adminOnly: true }
  },
  {
    path: 'admin/users',
    loadComponent: () =>
      import('./features/users/user-list/user-list.component').then(m => m.UserListComponent),
    canActivate: [authGuard],
    data: { adminOnly: true }
  },
  {
    path: 'admin/tier-config',
    loadComponent: () =>
      import('./features/admin/tier-config/tier-config.component').then(m => m.TierConfigComponent),
    canActivate: [authGuard],
    data: { adminOnly: true }
  },
  {
    path: 'manager',
    loadComponent: () =>
      import('./features/manager/manager-layout/manager-layout.component').then(m => m.ManagerLayoutComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['SALES_DIRECTOR', 'SALES_MANAGER'] },
    children: [
      { path: '', redirectTo: 'ticket-types', pathMatch: 'full' },
      {
        path: 'stages',
        loadComponent: () =>
          import('./features/stages/stage-list/stage-list.component').then(m => m.StageListComponent)
      },
      {
        path: 'segments',
        loadComponent: () =>
          import('./features/prodaja/segmenti/segmenti.component').then(m => m.SegmentiComponent)
      },
      {
        path: 'ticket-types',
        loadComponent: () =>
          import('./features/manager/ticket-types/ticket-type-list/ticket-type-list.component').then(m => m.TicketTypeListComponent)
      },
      {
        path: 'ticket-types/new',
        loadComponent: () =>
          import('./features/manager/ticket-types/ticket-type-form/ticket-type-form.component').then(m => m.TicketTypeFormComponent)
      },
      {
        path: 'ticket-types/:id/edit',
        loadComponent: () =>
          import('./features/manager/ticket-types/ticket-type-form/ticket-type-form.component').then(m => m.TicketTypeFormComponent)
      },
      {
        path: 'ticket-types/:id/periods',
        loadComponent: () =>
          import('./features/manager/ticket-types/pricing-periods/pricing-periods.component').then(m => m.PricingPeriodsComponent)
      },
      {
        path: 'promotions',
        loadComponent: () =>
          import('./features/prodaja/promotions/promotions.component').then(m => m.PromotionsComponent)
      }
    ]
  },
  {
    path: 'shop',
    loadComponent: () =>
      import('./features/shop/shop-layout/shop-layout.component').then(m => m.ShopLayoutComponent),
    canActivate: [authGuard],
    data: { roles: ['BUYER'] },
    children: [
      { path: '', redirectTo: 'tickets', pathMatch: 'full' },
      {
        path: 'tickets',
        loadComponent: () =>
          import('./features/shop/shop-tickets/shop-tickets.component').then(m => m.ShopTicketsComponent)
      },
      {
        path: 'checkout/:ticketTypeId',
        loadComponent: () =>
          import('./features/shop/checkout/checkout.component').then(m => m.CheckoutComponent)
      },
      {
        path: 'my-tickets',
        loadComponent: () =>
          import('./features/shop/my-tickets/my-tickets.component').then(m => m.MyTicketsComponent)
      }
    ]
  },
  {
    path: 'event-organization',
    loadComponent: () =>
      import('./features/event-organization/event-organization.component').then(m => m.EventOrganizationComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['EVENT_ORGANIZER'] }
  },
  {
    path: 'director/festivals',
    loadComponent: () =>
      import('./features/festival-director/director-festival-list/director-festival-list.component').then(m => m.DirectorFestivalListComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'] }
  },
  {
    path: 'director/festivals/:festivalId/campaign/new',
    loadComponent: () =>
      import('./features/festival-director/campaign-form/campaign-form.component').then(m => m.CampaignFormComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'] }
  },
  {
    path: 'director/festivals/:festivalId/campaign',
    loadComponent: () =>
      import('./features/festival-director/campaign-details/campaign-details.component').then(m => m.CampaignDetailsComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'] }
  },
  {
    path: 'director/festivals/:festivalId/campaign/ads/:adId',
    loadComponent: () =>
      import('./features/ad-review/ad-review-overview/ad-review-overview.component').then(m => m.AdReviewOverviewComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'], audience: 'director' }
  },
  {
    path: 'director/festivals/:festivalId/campaign/ads/:adId/versions/:versionNumber',
    loadComponent: () =>
      import('./features/ad-review/ad-version-detail/ad-version-detail.component').then(m => m.AdVersionDetailComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'], audience: 'director' }
  },
  {
    path: 'director/statistics',
    loadComponent: () =>
      import('./features/statistics/statistics-dashboard/statistics-dashboard.component').then(m => m.StatisticsDashboardComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_DIRECTOR'], audience: 'director' }
  },
  {
    path: 'manager/festivals',
    loadComponent: () =>
      import('./features/festival-manager/manager-festival-list/manager-festival-list.component').then(m => m.ManagerFestivalListComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'manager/festivals/:festivalId/campaign',
    loadComponent: () =>
      import('./features/festival-manager/manager-campaign-workspace/manager-campaign-workspace.component').then(m => m.ManagerCampaignWorkspaceComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'manager/festivals/:festivalId/campaign/ads/:adId',
    loadComponent: () =>
      import('./features/ad-review/ad-review-overview/ad-review-overview.component').then(m => m.AdReviewOverviewComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'], audience: 'manager' }
  },
  {
    path: 'manager/festivals/:festivalId/campaign/ads/:adId/edit',
    loadComponent: () =>
      import('./features/festival-manager/ad-form/ad-form.component').then(m => m.AdFormComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'manager/festivals/:festivalId/campaign/ads/:adId/versions/:versionNumber',
    loadComponent: () =>
      import('./features/ad-review/ad-version-detail/ad-version-detail.component').then(m => m.AdVersionDetailComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'], audience: 'manager' }
  },
  {
    path: 'manager/statistics',
    loadComponent: () =>
      import('./features/statistics/statistics-dashboard/statistics-dashboard.component').then(m => m.StatisticsDashboardComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'], audience: 'manager' }
  },
  {
    path: 'manager/campaigns/:campaignId/ads/new',
    loadComponent: () =>
      import('./features/festival-manager/ad-form/ad-form.component').then(m => m.AdFormComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'manager/campaigns/:campaignId/ad-types/new',
    loadComponent: () =>
      import('./features/festival-manager/ad-type-form/ad-type-form.component').then(m => m.AdTypeFormComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'manager/campaigns/:campaignId/ad-phases/new',
    loadComponent: () =>
      import('./features/festival-manager/ad-phase-form/ad-phase-form.component').then(m => m.AdPhaseFormComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['FESTIVAL_MANAGER'] }
  },
  {
    path: 'creative/campaigns',
    loadComponent: () =>
      import('./features/creative-work/creative-ad-list/creative-ad-list.component').then(m => m.CreativeAdListComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['PRODUCT_DESIGNER', 'TECHNICAL_SUPPORT'] }
  },
  {
    path: 'creative/campaigns/:campaignId',
    loadComponent: () =>
      import('./features/creative-work/creative-campaign-ads/creative-campaign-ads.component').then(m => m.CreativeCampaignAdsComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['PRODUCT_DESIGNER', 'TECHNICAL_SUPPORT'] }
  },
  {
    path: 'creative/campaigns/:campaignId/ads/:adId',
    loadComponent: () =>
      import('./features/creative-work/creative-ad-editor/creative-ad-editor.component').then(m => m.CreativeAdEditorComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['PRODUCT_DESIGNER', 'TECHNICAL_SUPPORT'] }
  },
  { path: 'creative/ads', redirectTo: 'creative/campaigns', pathMatch: 'full' },
{
    path: 'negotiation-manager',
    component: NegotiationManagerLayoutComponent,
    canActivate: [authGuard],
    data: { festivalRoles: ['NEGOTIATION_MANAGER'] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      
      // Dashboard Component
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/negotiation-manager/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      
      // Performers Modul
      {
        path: 'performers',
        loadComponent: () =>
          import('./features/negotiation-manager/performers/performer-list/performer-list.component').then(m => m.PerformerListComponent)
      },
      {
        path: 'performers/new',
        loadComponent: () =>
          import('./features/negotiation-manager/performers/performer-form/performer-form.component').then(m => m.PerformerFormComponent)
      },
      {
        path: 'performers/:id',
        loadComponent: () =>
          import('./features/negotiation-manager/performers/performer-detail/performer-detail.component').then(m => m.PerformerDetailComponent)
      },
      {
        path: 'performers/:id/edit',
        loadComponent: () =>
          import('./features/negotiation-manager/performers/performer-form/performer-form.component').then(m => m.PerformerFormComponent)
      },

      // Offers Modul
      {
        path: 'offers',
        loadComponent: () =>
          import('./features/negotiation-manager/offers/offer-list/offer-list.component').then(m => m.OfferListComponent)
      },
      {
        path: 'offers/new',
        loadComponent: () =>
          import('./features/negotiation-manager/offers/offer-form/offer-form.component').then(m => m.OfferFormComponent)
      },
      {
        path: 'offers/:offerId',
        loadComponent: () =>
          import('./features/negotiation-manager/offers/offer-detail/offer-detail.component').then(m => m.OfferDetailComponent)
      },
      {
        path: 'offers/:offerId/edit',
        loadComponent: () =>
          import('./features/negotiation-manager/offers/offer-form/offer-form.component').then(m => m.OfferFormComponent)
      },

      // Workflow template Modul

      {
        path: 'workflow-templates',
        loadComponent: () =>
          import('./features/negotiation-manager/workflow-template/workflow-template-list/workflow-template-list.component')
            .then(m => m.WorkflowTemplateListComponent)
      },
      {
        path: 'workflow-templates/new',
        loadComponent: () =>
          import('./features/negotiation-manager/workflow-template/workflow-template-form/workflow-template-form.component')
            .then(m => m.WorkflowTemplateFormComponent)
      },
      {
        path: 'workflow-templates/:id',
        loadComponent: () =>
          import('./features/negotiation-manager/workflow-template/workflow-template-detail/workflow-template-detail.component')
            .then(m => m.WorkflowTemplateDetailComponent)
      }
    ]
  },
  { path: '**', redirectTo: '/dashboard' }
];
