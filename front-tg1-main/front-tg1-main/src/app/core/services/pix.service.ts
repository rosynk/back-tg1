import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Pix {
  id?: number;
  contaOrigem: number;
  chaveDestino: string;
  valor: number;
  descricao: string;
  dataPix?: string;
}

export interface ChavePix {
  id?: number;
  usuario: number;
  tipo: string; // CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA
  chave: string;
  dataCriacao?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PixService {
  private apiUrl = 'http://localhost:8086/api/pix';

  constructor(private http: HttpClient) {}

  enviarPix(pix: Pix): Observable<Pix> {
    return this.http.post<Pix>(this.apiUrl, pix);
  }

  buscarPixPorId(id: number): Observable<Pix> {
    return this.http.get<Pix>(`${this.apiUrl}/${id}`);
  }

  buscarPixDaConta(idConta: number): Observable<Pix[]> {
    return this.http.get<Pix[]>(`${this.apiUrl}/conta/${idConta}`);
  }

  criarChavePix(chave: ChavePix): Observable<ChavePix> {
    return this.http.post<ChavePix>(`${this.apiUrl}/chaves`, chave);
  }

  listarChavesDoUsuario(idUsuario: number): Observable<ChavePix[]> {
    return this.http.get<ChavePix[]>(`${this.apiUrl}/chaves/usuario/${idUsuario}`);
  }

  deletarChavePix(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/chaves/${id}`);
  }
}
