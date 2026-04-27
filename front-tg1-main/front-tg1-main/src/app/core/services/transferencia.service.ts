import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Transferencia {
  id?: number;
  contaOrigem: number;
  contaDestino: number;
  valor: number;
  descricao: string;
  dataTransferencia?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TransferenciaService {
  private apiUrl = 'http://localhost:8086/api/transferencias';

  constructor(private http: HttpClient) {}

  realizarTransferencia(transferencia: Transferencia): Observable<Transferencia> {
    return this.http.post<Transferencia>(this.apiUrl, transferencia);
  }

  buscarPorId(id: number): Observable<Transferencia> {
    return this.http.get<Transferencia>(`${this.apiUrl}/${id}`);
  }

  buscarTodasDaConta(idConta: number): Observable<Transferencia[]> {
    return this.http.get<Transferencia[]>(`${this.apiUrl}/conta/${idConta}`);
  }

  buscarEnviadas(idConta: number): Observable<Transferencia[]> {
    return this.http.get<Transferencia[]>(`${this.apiUrl}/enviadas/conta/${idConta}`);
  }

  buscarRecebidas(idConta: number): Observable<Transferencia[]> {
    return this.http.get<Transferencia[]>(`${this.apiUrl}/recebidas/conta/${idConta}`);
  }

  exportarExtrato(idConta: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/exportar/conta/${idConta}`, { responseType: 'blob' });
  }
}
