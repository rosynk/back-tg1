import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';

/**
 * Guardian com Inspeção de Tokens e Roles
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      const token = authService.getToken();
      const requiredRoles = route.data['roles'] as Array<string>;

      console.log('--- 🛡️ INSPEÇÃO DO GUARDIAN ---');
      console.log('📍 Rota Alvo:', state.url);
      console.log('🔑 Token Encontrado:', token ? 'SIM (Inicia com: ' + token.substring(0, 15) + '...)' : 'NÃO');

      // 1. Verificação de Autenticação Básica
      if (!token) {
        console.error('🛑 [Guard] Acesso Bloqueado: Usuário não possui Token.');
        router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
      }

      // 2. Recuperação do Usuário (Plano B caso o Subject esteja nulo)
      let currentUser = user;
      if (!currentUser && token) {
        console.warn('⚠️ [Guard] Usuário nulo no Subject, tentando reidratar via Token...');
        // Forçamos a decodificação se o estado sumiu
        authService['decodeAndSetUser'](token);
        currentUser = authService.getCurrentUser();
      }

      console.log('👤 Dados do Usuário no Sistema:', currentUser);

      // 3. Validação de Permissões (Roles)
      if (!requiredRoles || requiredRoles.length === 0) {
        console.log('✅ [Guard] Acesso Liberado: Rota pública ou sem restrição de Role.');
        return true;
      }

      console.log('📋 Roles Necessárias para esta rota:', requiredRoles);
      console.log('🎫 Role que o Usuário possui:', currentUser?.role);

      const hasRole = currentUser && currentUser.role && requiredRoles.includes(currentUser.role);

      if (hasRole) {
        console.log('✅ [Guard] Acesso Autorizado! Role compatível.');
        return true;
      }

      // 4. Tratamento de Erro de Permissão
      console.error('🚫 [Guard] Acesso Negado: O usuário logado não tem a permissão necessária.');

      // Se ele está logado mas a role é errada, mandamos para o dashboard (ou 403)
      // para evitar o loop infinito de voltar para o login.
      if (currentUser) {
        router.navigate(['/dashboard']);
      } else {
        router.navigate(['/login']);
      }

      return false;
    })
  );
};
