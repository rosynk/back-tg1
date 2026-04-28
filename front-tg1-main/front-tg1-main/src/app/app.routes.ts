import { Routes } from '@angular/router';
import { WelcomeComponent } from './componentes/welcome/welcome.component';
import { LoginComponent } from './componentes/login/login.component';
import { SignInComponent } from './componentes/sign-in/sign-in.component';
import { DashboardComponent } from './componentes/dashboard/dashboard.component';
import { TransferenciaComponent } from './componentes/transferenciaTed/transferencia.component'; // ✅ Importe aqui
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // 1. Redirecionamento Inicial
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },

  // 2. Rotas Públicas (Acesso livre)
  { path: 'welcome', component: WelcomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'sign-in', component: SignInComponent },

  // 3. Rotas Protegidas (Exigem Login e Token JWT)
 {
  path: 'dashboard',
  component: DashboardComponent,
  canActivate: [authGuard],
  title: 'Dashboard - Bizi Bank', // Opcional, para a aba do navegador
  data: { roles: ['ROLE_CLIENTE', 'ROLE_ADMIN'] } // Adicione isso aqui!
},

  {
  path: 'transferencia',
  component: TransferenciaComponent,
  canActivate: [authGuard],
  title: 'Transferência - Bizi Bank', // O Angular atualizará a aba do navegador para isso
  data: {
    roles: ['ROLE_CLIENTE', 'ROLE_ADMIN'],
    animation: 'TransferPage' // Exemplo de outra propriedade comum
  }
},
  // 4. Rota de Wildcard
  { path: '**', redirectTo: '/welcome' }
];
