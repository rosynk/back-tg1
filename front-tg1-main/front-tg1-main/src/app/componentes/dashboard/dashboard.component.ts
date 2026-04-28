
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

// Interfaces para tipagem dos dados vindos do banco BiziBanco
interface Transacao {
  data: string;
  tipo: string;
  valor: number;
  detalhes: string;
}

interface Extrato {
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
  extrato: Extrato | null = null;
  conta: Conta | null = null; // Tipado corretamente
  loading = true;
  erro = '';

  private readonly API_BASE = 'http://localhost:8086/api';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.loading = true;

    // ✅ Chamada simplificada: O Interceptor anexa o Token automaticamente
    this.http.get<Extrato>(`${this.API_BASE}/extrato`).subscribe({
      next: (data) => {
        this.extrato = data;
        this.loading = false;
        console.log('✅ Dados do extrato carregados com sucesso');
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403 || err.status === 401) {
          this.erro = 'Sessão expirada. Redirecionando...';
          setTimeout(() => this.router.navigate(['/login']), 2000);
        } else {
          this.erro = 'Não foi possível carregar os dados financeiros.';
        }
        console.error('❌ Erro na requisição:', err);
      }
    });

    // Chamada de contas também simplificada
    this.http.get<Conta[]>(`${this.API_BASE}/contas`).subscribe({
      next: (data) => {
        this.conta = data.length > 0 ? data[0] : null;
      },
      error: (err) => console.error('Erro ao buscar contas:', err)
    });
  }
}
