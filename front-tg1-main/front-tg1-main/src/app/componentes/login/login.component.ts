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
    private loginService: LoginService,
    private router: Router
  ) {
    this.formAdm = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]]
    });

    this.formCliente = this.fb.nonNullable.group({
      cpf: ['', [Validators.required, Validators.minLength(11)]],
      senha: ['', [Validators.required]]
    });
  }

  enviarAdm(): void {
    if (this.formAdm.invalid) return;

    this.loading = true;
    this.loginService.login(this.formAdm.getRawValue()).subscribe({
      next: (res: any) => {
        localStorage.clear();
        localStorage.setItem('token', res.token);

        const usuarioLogado = {
          id: res.email || this.formAdm.getRawValue().email,
          email: res.email || this.formAdm.getRawValue().email,
          role: res.role || 'ROLE_ADMIN'
        };
        localStorage.setItem('user', JSON.stringify(usuarioLogado));

        console.log('✅ Admin autenticado');
        this.router.navigate(['/welcome']);
      },
      error: (err) => {
        this.mensagem = 'Erro no login administrativo.';
        this.loading = false;
      }
    });
  }

  enviarCliente(): void {
    if (this.formCliente.invalid) return;

    this.loading = true;
    const dados = this.formCliente.getRawValue();
    const cpfLimpo = dados.cpf.replace(/\D/g, '');

    this.loginService.loginCliente({ cpf: cpfLimpo, senha: dados.senha }).subscribe({
      next: (res: any) => {
        // MANTIDO EXATAMENTE COMO ESTAVA
        localStorage.clear();
        localStorage.setItem('token', res.token);

        const usuarioLogado = {
          id: cpfLimpo,
          email: res.email || '',
          nome: res.nome || 'Usuário',
          role: res.role || 'ROLE_CLIENTE'
        };

        localStorage.setItem('user', JSON.stringify(usuarioLogado));
        console.log('✅ Cliente autenticado com CPF:', cpfLimpo);

        // Navegação direta para o dashboard
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.mensagem = "CPF ou senha incorretos.";
      }
    });
  }

  navegarParaSignIn(): void {
    this.router.navigate(['/cadastro']);
  }
}
