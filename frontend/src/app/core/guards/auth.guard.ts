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
  const adminOnly = route.data?.['adminOnly'] === true;
  const allowedFestivalRoles: string[] | undefined = route.data?.['festivalRoles'];

  if (adminOnly && !isAdmin) {
    return router.createUrlTree(['/pending']);
  }

  // If the route declares required festival roles, block anyone whose
  // assignment role is not in that list — including users with no assignment.
  if (allowedFestivalRoles) {
    const festivalRole = user?.assignment?.festivalRole;
    if (!festivalRole || !allowedFestivalRoles.includes(festivalRole)) {
      return router.createUrlTree(['/pending']);
    }
  }

  if (route.routeConfig?.path === 'pending' && isAdmin) {
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
