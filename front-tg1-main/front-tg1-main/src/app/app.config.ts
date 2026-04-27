import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors, withFetch } from '@angular/common/http';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // 1. Otimização da detecção de mudanças (padrão Angular 18)
    provideZoneChangeDetection({ eventCoalescing: true }),

    // 2. Configuração de Rotas com binding de parâmetros e transições suaves
    provideRouter(
      routes,
      withComponentInputBinding(), // Permite receber parâmetros da URL como @Input
      withViewTransitions()        // Adiciona transições suaves entre páginas
    ),

    // 3. Configuração do Cliente HTTP
    provideHttpClient(
      // ✅ Essencial para o seu projeto: Registra o interceptor do Token
      withInterceptors([jwtInterceptor]),

      // ✅ Habilita o uso da API 'fetch' (mais moderna e performática que o XHR antigo)
      withFetch()
    )
  ]
};
