import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Verifica se a requisição é para o seu servidor para evitar enviar token para APIs externas
  const isApiUrl = req.url.startsWith('http://localhost:8086');

  if (token && isApiUrl) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('🚀 Interceptor: Token anexado para:', req.url);
  }

  return next(req);
};
