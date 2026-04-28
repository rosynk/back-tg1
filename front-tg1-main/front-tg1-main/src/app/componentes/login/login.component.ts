import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from '../../core/services/login.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  formAdm;
  formCliente;
  mensagem: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService, // Centralizado para loginService
    private router: Router
  ) {
    // Form para Admin (Mantive caso você ainda use para suporte interno)
    this.formAdm = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]]
    });

    // 🏦 FORM DO CLIENTE (Foco total no CPF)
    this.formCliente = this.fb.nonNullable.group({
      cpf: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
      senha: ['', [Validators.required]]
    });
  }

  enviarAdm(): void {
    if (this.formAdm.invalid) return;
    this.loading = true;

    this.loginService.login(this.formAdm.getRawValue()).subscribe({
      next: (res: any) => {
        this.processarSucessoLogin(res, '/welcome');
      },
      error: () => {
        this.mensagem = 'Erro no login administrativo.';
        this.loading = false;
      }
    });
  }

  // 🛡️ O MÉTODO DO CLIENTE QUE VOCÊ QUERIA
  enviarCliente(): void {
    if (this.formCliente.invalid) {
      alert('Por favor, preencha o CPF e a senha corretamente.');
      return;
    }

    this.loading = true;
    // Extraímos o CPF e Senha direto do formulário reativo
    const { cpf, senha } = this.formCliente.getRawValue();

    console.log(`📡 [Bizi Bank] Iniciando autenticação para CPF: ${cpf}...`);

    this.loginService.login({ cpf, senha }).subscribe({
      next: (res: any) => {
        console.log('✅ [Login] Credenciais aceitas pelo Java!');

        // Salvamos os dados e navegamos
        this.processarSucessoLogin(res, '/dashboard');
      },
      error: (err) => {
        console.error('❌ [Login] Falha na autenticação:', err);
        this.mensagem = 'CPF ou Senha incorretos.';
        this.loading = false;
        alert('Dados inválidos. Verifique seu CPF e senha.');
      }
    });
  }

  /**
   * Método auxiliar para padronizar o salvamento de sessão
   */
  private processarSucessoLogin(res: any, rotaDestino: string): void {
    // Limpa lixos de sessões anteriores
    localStorage.clear();

    // Salva o token
    localStorage.setItem('token', res.token);

    // O objeto de usuário agora usa o CPF como ID (res.cpf vem do seu backend)
    const usuarioLogado = {
      id: res.cpf || res.sub,
      nome: res.nome || 'Cliente Bizi',
      role: res.role || 'ROLE_USER'
    };

    localStorage.setItem('user', JSON.stringify(usuarioLogado));

    console.log('🚀 Sessão preparada. Navegando...');

    // Timeout para garantir que o LocalStorage gravou antes do Guard rodar
    setTimeout(() => {
      this.router.navigate([rotaDestino]);
    }, 100);
  }

  navegarParaSignIn(): void {
    this.router.navigate(['/cadastro']);
  }
}
