import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

// 1. GARANTA QUE AS INTERFACES ESTÃO AQUI NO TOPO
interface Transacao {
  data: string;
  tipo: string;
  valor: number;
  detalhes: string;
}

interface Extrato { // <--- Verifique se o nome está exatamente assim
  titular: string;
  saldoAtual: number;
  transacoes: Transacao[];
}

interface Conta {
  id: number;
  usuarioId: number;
  tipoConta: string;
  numeroAgencia: string;
  numeroConta: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  // 2. Agora o TypeScript vai reconhecer o tipo Extrato aqui
  extrato: Extrato | null = null;
  conta: any = null;
  loading = true;
  erro = '';

  private readonly API_BASE = 'http://localhost:8086/api';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.loading = true;
    const token = localStorage.getItem('token');

    // Se não tiver token, nem tenta a requisição e volta pro login
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    // Busca Extrato
    this.http.get<Extrato>(`${this.API_BASE}/extrato`, { headers }).subscribe({
      next: (data) => {
        this.extrato = data;
        this.loading = false;
      },
      error: (err) => {
        this.erro = 'Erro ao carregar extrato.';
        this.loading = false;
      }
    });

    // Busca Contas (O Java retorna uma lista, então usamos <any[]> ou <Conta[]>)
    this.http.get<Conta[]>(`${this.API_BASE}/contas`, { headers }).subscribe({
      next: (data) => {
        this.conta = data.length > 0 ? data[0] : null;
      },
      error: (err) => console.error('Erro ao carregar contas', err)
    });
  }
}
