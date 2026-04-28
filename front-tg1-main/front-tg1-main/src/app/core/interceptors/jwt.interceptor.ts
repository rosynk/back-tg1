import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Se a URL for para o seu backend (porta 8086)
  const isApiUrl = req.url.includes(':8086') || req.url.startsWith('/api');

  if (token && isApiUrl) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('🚀 [Interceptor] Token anexado para:', req.url);
  } else if (!token && isApiUrl) {
    console.warn('🚨 [Interceptor] Tentativa de acesso à API sem Token!');
  }

  return next(req);
};
