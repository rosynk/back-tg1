import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export interface User {
  id: string;   // Mantido como string para suportar CPF e Email
  email: string;
  nome: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8086/api/auth';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.loadUser();
    }
  }

  login(email: string, senha: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, senha }).pipe(
      tap(response => {
        if (response && response.token) {
          this.setSession(response.token);
        }
      }),
      catchError(err => {
        console.error('❌ Erro no processo de login:', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Centraliza a gravação do token e decodificação do usuário
   */
  private setSession(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', token);
      this.decodeAndSetUser(token);
    }
  }

  private decodeAndSetUser(token: string): void {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) throw new Error('JWT malformatado');

      // Decodifica o payload (Base64)
      const payload = JSON.parse(atob(parts[1]));

      console.log('🔍 Debug Payload JWT:', payload);

      /**
       * 🔥 ESTRATÉGIA ANTI-QUEBRA:
       * 1. Prioriza o CPF (id para transações bancárias)
       * 2. Fallback para o SUB (email) caso o CPF não exista (ex: Admin)
       */
      const userId = payload.cpf || payload.sub;

      if (!userId) {
        throw new Error('Identificador de usuário não encontrado no token');
      }

      const user: User = {
        id: String(userId),
        email: payload.sub, // O e-mail geralmente vem no 'sub'
        nome: payload.nome || 'Usuário',
        role: payload.role || payload.roles || ''
      };

      this.currentUserSubject.next(user);

      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('user', JSON.stringify(user));
      }

      console.log('✅ Usuário autenticado com ID:', user.id);

    } catch (error) {
      console.error('⚠️ Falha crítica ao processar token:', error);
      this.logout(); // Limpa tudo para evitar estado inconsistente
    }
  }

  private loadUser(): void {
    const token = this.getToken();
    if (token) {
      this.decodeAndSetUser(token);
    }
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  isAuthenticated(): boolean {
  const token = this.getToken();
  if (!token) return false;

  // Se o token existe mas o usuário sumiu do Subject (comum em redirects rápidos)
  if (!this.currentUserSubject.value) {
    this.decodeAndSetUser(token); // Tenta recuperar na hora
  }

  return !!this.currentUserSubject.value;
}

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
    this.currentUserSubject.next(null);
  }
}
