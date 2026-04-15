import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, tap, throwError } from 'rxjs';
import { BackendStatusService } from '../services/backend-status.service';

export const backendErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const status = inject(BackendStatusService);
  return next(req).pipe(
    tap(() => status.isDown.set(false)),
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        status.isDown.set(true);
      }
      return throwError(() => error);
    })
  );
};
