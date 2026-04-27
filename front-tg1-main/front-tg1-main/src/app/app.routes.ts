import { Routes } from '@angular/router';
import { WelcomeComponent } from './componentes/welcome/welcome.component';
import { LoginComponent } from './componentes/login/login.component';
import { SignInComponent } from './componentes/sign-in/sign-in.component';
import { DashboardComponent } from './componentes/dashboard/dashboard.component';
import { authGuard } from './core/guards/auth.guard'; // Caminho conforme sua imagem

export const routes: Routes = [
  // 1. Redirecionamento Inicial
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },

  // 2. Rotas Públicas
  { path: 'welcome', component: WelcomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'sign-in', component: SignInComponent },

  // 3. Rotas Protegidas (Exigem Login)
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  /* Se futuramente você criar um AdminComponent, use assim:
     {
       path: 'admin',
       component: AdminComponent,
       canActivate: [authGuard],
       data: { roles: ['ADMIN'] }
     },
  */

  // 4. Rota de Wildcard (URL não encontrada volta para Welcome)
  { path: '**', redirectTo: '/welcome' }
];
