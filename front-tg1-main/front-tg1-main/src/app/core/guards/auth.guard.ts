import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * AuthGuard Corrigido e Otimizado
 * Garante que o usuário esteja autenticado e possua as permissões (roles) necessárias.
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Verifica se existe um token válido (e não expirado, se você tiver essa lógica)
  if (authService.isAuthenticated()) {
    const user = authService.getCurrentUser();
    const requiredRoles = route.data['roles'] as Array<string>;

    // Debug opcional no console do navegador (F12)
    console.log(`🛡️ [Guard] Acessando: ${state.url}`);
    console.log('👤 [Guard] Usuário Atual:', user);
    console.log('🔑 [Guard] Permissões Requeridas:', requiredRoles);

    // 2. Se a rota não exige nenhuma Role específica, o acesso é liberado
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    // 3. Verifica se o usuário possui a Role necessária
    // DICA: Certifique-se de que o backend envia a string EXATA (ex: 'ADMIN' ou 'USER')
    if (user && user.role && requiredRoles.includes(user.role)) {
      return true;
    }

    // 4. Caso logado mas sem a permissão correta
    console.warn(`🚫 [Guard] Acesso negado para a Role: ${user?.role}`);

    // Redireciona para o dashboard ou uma página de "não autorizado"
    router.navigate(['/dashboard']);
    return false;
  }

  // 5. Se não estiver autenticado, redireciona para o login
  // Opcional: Salvar a URL que o usuário tentou acessar para redirecionar após o login
  console.error('🛑 [Guard] Usuário não autenticado. Redirecionando para login...');

  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });

  return false;
};
