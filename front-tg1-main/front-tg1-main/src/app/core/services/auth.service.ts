import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

export interface User {
  id: number;
  email: string;
  nome: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8086/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    if (typeof window !== 'undefined') {
      this.loadUser();
    }
  }

  login(email: string, senha: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, senha }).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          this.decodeAndSetUser(response.token);
        }
      }),
      catchError(err => {
        console.error('Erro no processo de login:', err);
        return throwError(() => err);
      })
    );
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private decodeAndSetUser(token: string): void {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) throw new Error('JWT malformatado');

      const payload = JSON.parse(atob(parts[1]));
      const user: User = {
        id: payload.sub,
        email: payload.email || payload.sub,
        nome: payload.nome || 'Usuário',
        role: payload.role || ''
      };
      this.currentUserSubject.next(user);
    } catch (error) {
      console.error('Erro na decodificação:', error);
      this.logout();
    }
  }

  private loadUser(): void {
    const token = this.getToken();
    if (token) this.decodeAndSetUser(token);
  }
}
