import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Extrato {
  id?: number;
  conta: number;
  dataInicio: string;
  dataFim: string;
  saldoInicial: number;
  saldoFinal: number;
  transacoes?: any[];
}

@Injectable({
  providedIn: 'root'
})
export class ExtratoService {
  private apiUrl = 'http://localhost:8086/api/extratos';

  constructor(private http: HttpClient) {}

  gerarExtrato(idConta: number): Observable<Extrato> {
    return this.http.get<Extrato>(`${this.apiUrl}/conta/${idConta}`);
  }

  gerarExtratoFiltrado(
    idConta: number,
    dataInicio: string,
    dataFim: string,
    tipo?: string
  ): Observable<Extrato> {
    let params = new HttpParams()
      .set('dataInicio', dataInicio)
      .set('dataFim', dataFim);

    if (tipo) {
      params = params.set('tipo', tipo);
    }

    return this.http.get<Extrato>(`${this.apiUrl}/conta/${idConta}/filtrado`, { params });
  }

  exportarPDF(idConta: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conta/${idConta}/pdf`, { responseType: 'blob' });
  }

  exportarExcel(idConta: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conta/${idConta}/excel`, { responseType: 'blob' });
  }
}
