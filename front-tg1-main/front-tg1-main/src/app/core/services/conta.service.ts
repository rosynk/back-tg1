import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ContaService {
  private apiUrl = 'http://localhost:8086/api/contas';

  constructor(private http: HttpClient) {}

  listarContasDoUsuario(idUsuario: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/usuario/${idUsuario}`);
  }

  obterSaldo(id: number): Observable<{ saldo: number }> {
    return this.http.get<{ saldo: number }>(`${this.apiUrl}/${id}/saldo`);
  }

  criarConta(conta: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, conta);
  }
}
