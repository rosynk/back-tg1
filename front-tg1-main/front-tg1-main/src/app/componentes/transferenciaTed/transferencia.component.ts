import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-transferencia',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './transferencia.component.html',
  styleUrl: './transferencia.component.css'
})
export class TransferenciaComponent implements OnInit {
  extrato: any = null;
  conta: any = null;

  // Propriedades de controle de estado (UI)
  loading = false;          // Corrigindo o erro ngtsc(2339)
  loadingTransfer = false;
  msgSucesso = '';
  msgErro = '';

  // Objeto de dados para o formulário (TED JSON)
  transferenciaData = {
    contaOrigem: 0,
    agenciaDestino: '',
    numeroContaDestino: '',
    valor: null as number | null
  };

  private readonly API_BASE = 'http://localhost:8086/api';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.carregarDadosIniciais();
  }

  carregarDadosIniciais() {
    this.loading = true; // Inicia o estado de carregamento global

    // 1. Busca extrato para saldo e transações
    this.http.get<any>(`${this.API_BASE}/transacoes/extrato`).subscribe({
      next: (data) => {
        this.extrato = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar extrato', err);
        this.loading = false;
      }
    });

    // 2. Busca dados da conta origem
    this.http.get<any[]>(`${this.API_BASE}/contas`).subscribe({
      next: (contas) => {
        if (contas && contas.length > 0) {
          this.conta = contas[0];
          this.transferenciaData.contaOrigem = this.conta.id;
        }
      },
      error: (err) => console.error('Erro ao carregar conta', err)
    });
  }

  enviarTransferencia() {
    if (!this.transferenciaData.agenciaDestino || !this.transferenciaData.numeroContaDestino) {
      this.msgErro = 'Preencha os dados de destino.';
      return;
    }

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
        this.carregarDadosIniciais(); // Atualiza o saldo e a lista de atividades
      },
      error: (err) => {
        this.msgErro = err.error?.mensagem || 'Erro ao realizar transferência.';
        this.loadingTransfer = false;
      }
    });
  }

  // Método solicitado para o botão da Sidebar
  testeNavegacao() {
    console.log('Navegação funcionando corretamente!');
    alert('Sistema de navegação Bizi Bank ativo.');
  }

  private limparFormulario() {
    this.transferenciaData.valor = null;
    this.transferenciaData.agenciaDestino = '';
    this.transferenciaData.numeroContaDestino = '';
  }
}
