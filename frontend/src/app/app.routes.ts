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
    path: 'sales',
    loadComponent: () =>
      import('./features/prodaja/prodaja-layout/prodaja-layout.component').then(m => m.ProdajaLayoutComponent),
    canActivate: [authGuard],
    data: { festivalRoles: ['SALES_DIRECTOR'] },
    children: [
      { path: '', redirectTo: 'stages', pathMatch: 'full' },
      {
        path: 'stages',
        loadComponent: () =>
          import('./features/stages/stage-list/stage-list.component').then(m => m.StageListComponent)
      },
      {
        path: 'segments',
        loadComponent: () =>
          import('./features/prodaja/segmenti/segmenti.component').then(m => m.SegmentiComponent)
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
  { path: '**', redirectTo: '/dashboard' }
];
