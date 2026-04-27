package bizi.com.demo.validacoes;

import org.springframework.stereotype.Service;

import bizi.com.demo.usuario.UsuarioDto;
import bizi.com.demo.validacoes.CPF.CPFValidador;
import bizi.com.demo.validacoes.email.EmailValidator;
import bizi.com.demo.validacoes.telefone.TelefoneValidator;




@Service
public class ValidationService {

    private  CPFValidador cpfValidator;
    private  EmailValidator emailValidator;
    private  TelefoneValidator telefoneValidator;

    public ValidationService(CPFValidador cpfValidator,
                             EmailValidator emailValidator,
                             TelefoneValidator telefoneValidator) {

        this.cpfValidator = cpfValidator;
        this.emailValidator = emailValidator;
        this.telefoneValidator = telefoneValidator;
    }

    public void validarUsuario(UsuarioDto dto) {

        if (!cpfValidator.isValid(dto.getCpf())) {
            throw new RuntimeException("CPF inválido");
        }

        if (!emailValidator.isValid(dto.getEmail())) {
            throw new RuntimeException("Email inválido");
        }

        if (!telefoneValidator.isValid(dto.getTelefone())) {
            throw new RuntimeException("Telefone inválido");
        }
        
        
    }
    
 
}
