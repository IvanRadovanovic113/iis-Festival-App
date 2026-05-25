import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

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
  { path: '**', redirectTo: '/dashboard' }
];
