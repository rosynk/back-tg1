import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


interface Usuario {
  idUsuario: number;
  nomeCompleto: string;
  cpf: string;
}


interface Transacao {
  dataHora: string;      // No Java: private LocalDateTime dataHora
  tipoTransacao: string; // No Java: private TipoTransacao tipoTransacao
  valor: number;         // No Java: private BigDecimal valor
  cpfDestino?: string;   // Opcional: para mostrar pra quem ele mandou
}

interface Extrato {
  titular: string; saldoAtual: number; transacoes: Transacao[];
}

interface Conta {
  id: number;
  usuario: Usuario; // Objeto completo vindo do Backend
  tipoConta: string;
  numeroAgencia: string;
  numeroConta: string;
  saldo: number;    // Adicionado o campo saldo!
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  // --- Dados da Tela ---
  extrato: Extrato | null = null;
  conta: Conta | null = null;
  loading = true;
  erro = '';

  // --- Dados da Transferência ---
  transferenciaData = {
    numeroContaDestino: '',
    valor: null as number | null,
    tipo: 'TED',
    detalhes: '',
    idContaOrigem: 0
  };

  loadingTransfer = false;
  msgSucesso = '';
  msgErro = '';

  private readonly API_BASE = 'http://localhost:8086/api';

  // O Constructor deve aparecer apenas UMA vez dentro da classe
  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.carregarDados();
  }

  // MÉTODO PARA TESTAR A NAVEGAÇÃO
  testeNavegacao() {
    console.log("🚀 Botão clicado! Tentando navegar...");
    this.router.navigate(['/transferencia']).then(podeIr => {
      if(podeIr) {
        console.log("✅ Rota permitida!");
      } else {
        console.error("❌ Rota bloqueada pelo AuthGuard ou não existe!");
      }
    });
  }

  carregarDados() {
    this.loading = true;

    // 1. Busca Extrato
    this.http.get<Transacao[]>(`${this.API_BASE}/transacoes/extrato`).subscribe({
  next: (data) => {
    // Criamos o objeto que o seu HTML espera manualmente
    this.extrato = {
      titular: this.conta?.usuario?.nomeCompleto || '',
      saldoAtual: this.conta?.saldo || 0,
      transacoes: data // 'data' aqui já é a lista List<TransacaoModel> do Java
    };
    this.loading = false;
    console.log('✅ Extrato carregado com', data.length, 'itens');
  }
});

    // 2. Busca Conta
    this.http.get<Conta[]>(`${this.API_BASE}/contas`).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.conta = data[0];
          this.transferenciaData.idContaOrigem = this.conta.id;
        }
      },
      error: (err) => console.error('❌ Erro contas', err)
    });
  }

  enviarTransferencia() {
    if (!this.transferenciaData.valor || this.transferenciaData.valor <= 0) {
      this.msgErro = 'Informe um valor válido.';
      return;
    }

    this.loadingTransfer = true;
    this.msgSucesso = '';
    this.msgErro = '';

    this.http.post(`${this.API_BASE}/transferencias`, this.transferenciaData).subscribe({
      next: () => {
        this.msgSucesso = 'Transferência realizada com sucesso!';
        this.loadingTransfer = false;
        this.limparFormulario();
        this.carregarDados();
      },
      error: (err) => {
        this.msgErro = err.error?.mensagem || 'Erro na transferência.';
        this.loadingTransfer = false;
      }
    });
  }

  private limparFormulario() {
    this.transferenciaData.valor = null;
    this.transferenciaData.numeroContaDestino = '';
    this.transferenciaData.detalhes = '';
  }

  private tratarErro(err: any) {
    if (err.status === 403 || err.status === 401) {
      this.router.navigate(['/login']);
    } else {
      this.erro = 'Erro ao carregar dados.';
    }
  }
} // <--- Fim da Classe (Certifique-se de que nada ficou fora daqui)
