import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Interface para tipar a resposta do Backend (Opcional, mas resolve erros de 'any')
 */
export interface TokenResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  // URL base do seu backend Spring Boot (ajuste a porta se necessário)
  private readonly API = 'http://localhost:8086/api/auth';

  constructor(private http: HttpClient) { }

  /**
   * Método para o login padrão
   */
  login(payload: any): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.API}/login`, payload);
  }

  /**
   * Método específico para login de clientes (exigido pelo seu componente)
   * Resolve o erro: Property 'loginCliente' does not exist on type 'LoginService'
   */
  loginCliente(payload: any): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.API}/loginCliente`, payload);
  }

  /**
   * Solicitação de código para recuperação de senha
   */
  solicitarRecuperacao(email: string): Observable<any> {
    return this.http.post(`${this.API}/recuperar-senha`, { email });
  }

  /**
   * Redefinição final da senha com o código recebido
   */
  redefinirSenha(dados: any): Observable<any> {
    return this.http.post(`${this.API}/redefinir-senha`, dados);
  }
}
