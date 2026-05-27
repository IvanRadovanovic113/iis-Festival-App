import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const user = authService.getCurrentUser();
  const isAdmin = user?.role === 'ADMIN';
  const isBuyer = user?.role === 'BUYER';
  const festivalRole = user?.assignment?.festivalRole;
  const adminOnly = route.data?.['adminOnly'] === true;
  const allowedFestivalRoles: string[] | undefined = route.data?.['festivalRoles'];
  const allowedRoles: string[] | undefined = route.data?.['roles'];

  // Maps festival role to its home route
  const festivalHome = (fr: string | undefined): string => {
    if (fr === 'SALES_DIRECTOR' || fr === 'SALES_MANAGER') return '/manager';
    if (fr === 'NEGOTIATION_MANAGER') return '/negotiation-manager';
    return '/pending';
  };

  // Admin-only routes
  if (adminOnly && !isAdmin) {
    if (isBuyer) return router.createUrlTree(['/shop']);
    if (festivalRole) return router.createUrlTree([festivalHome(festivalRole)]);
    return router.createUrlTree(['/pending']);
  }

  // Routes restricted by system role (e.g. BUYER)
  if (allowedRoles) {
    if (!user?.role || !allowedRoles.includes(user.role)) {
      return router.createUrlTree([festivalRole ? festivalHome(festivalRole) : '/pending']);
    }
  }

  // Routes restricted by festival role (SALES_MANAGER, SALES_DIRECTOR, etc.)
  if (allowedFestivalRoles) {
    if (!festivalRole || !allowedFestivalRoles.includes(festivalRole)) {
      return router.createUrlTree([isBuyer ? '/shop' : '/pending']);
    }
  }

  // Admins don't belong on pending
  if (route.routeConfig?.path === 'pending' && isAdmin) {
    return router.createUrlTree(['/dashboard']);
  }

  // Buyers don't belong on pending
  if (route.routeConfig?.path === 'pending' && isBuyer) {
    return router.createUrlTree(['/shop']);
  }

  // Festival staff don't belong on pending
  if (route.routeConfig?.path === 'pending' && festivalRole) {
    return router.createUrlTree([festivalHome(festivalRole)]);
  }

  return true;
};
