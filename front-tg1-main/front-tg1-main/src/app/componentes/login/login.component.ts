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

  navegarParaSignIn(): void {
    this.router.navigate(['/cadastro']);
  }

  // ✅ CORREÇÃO ADMIN: Agora salva o papel (Role) para o Guard não dar NULL
  enviarAdm(): void {
    if (this.formAdm.invalid) {
      this.mensagem = "Preencha os dados de administrador.";
      return;
    }

    this.loginService.login(this.formAdm.getRawValue()).subscribe({
      next: (res: any) => {
        if (res.token) {
          localStorage.setItem('token', res.token);

          // Criamos o objeto de usuário com a ROLE_ADMIN
          // O seu AuthGuard vai ler exatamente este campo 'role'
          const usuarioLogado = {
            email: this.formAdm.getRawValue().email,
            role: res.role || 'ROLE_ADMIN'
          };

          localStorage.setItem('user', JSON.stringify(usuarioLogado));

          console.log('✅ Admin autenticado com sucesso!');
          this.router.navigate(['/welcome']);
        }
      },
      error: (err: any) => {
        this.mensagem = 'Credenciais administrativas inválidas.';
        console.error(err);
      }
    });
  }

  // ✅ CORREÇÃO CLIENTE: Também salva a ROLE_CLIENTE
  enviarCliente(): void {
    if (this.formCliente.invalid) {
      this.mensagem = "CPF ou senha inválidos.";
      return;
    }

    const dados = this.formCliente.getRawValue();
    const payload = {
      cpf: dados.cpf.replace(/\D/g, ''),
      senha: dados.senha
    };

    this.loginService.loginCliente(payload).subscribe({
      next: (res: any) => {
        if (res.token) {
          localStorage.setItem('token', res.token);

          const usuarioLogado = {
            cpf: payload.cpf,
            role: res.role || 'ROLE_CLIENTE'
          };

          localStorage.setItem('user', JSON.stringify(usuarioLogado));

          console.log('✅ Cliente autenticado com sucesso!');
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err: any) => {
        this.mensagem = "Erro ao conectar. Verifique seus dados.";
      }
    });
  }
}
