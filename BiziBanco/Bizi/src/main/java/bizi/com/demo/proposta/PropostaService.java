package bizi.com.demo.proposta;

import bizi.com.demo.contaBancaria.*;
import bizi.com.demo.usuario.*;
import bizi.com.demo.validacoes.CPF.CPFValidador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.Period;

@Service
public class PropostaService {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ContaBancariaService contaService;
    @Autowired private PropostaRepository propostaRepository;
    @Autowired private CPFValidador cpfValidador;

    @Transactional
    public PropostaResponseDto processarAbertura(PropostaRequestDto dto) {
        
        // 1. Validação de Idade
        int idade = Period.between(dto.getDataNascimento(), LocalDate.now()).getYears();
        if (idade < 18) {
            registrarProposta(dto, StatusProposta.NEGADA, "Usuário menor de idade", 0);
            throw new RuntimeException("Não é permitido abrir conta para menores de 18 anos.");
        }

        // 2. Validação de CPF
        if (!cpfValidador.isValid(dto.getCpf())) {
            registrarProposta(dto, StatusProposta.NEGADA, "CPF Inválido", 0);
            throw new RuntimeException("O CPF informado não é válido.");
        }

        try {
            // Conversão para UsuarioDto
            UsuarioDto userDto = new UsuarioDto();
            userDto.setNomeCompleto(dto.getNomeCompleto());
            userDto.setCpf(dto.getCpf());
            userDto.setEmail(dto.getEmail());
            userDto.setTelefone(dto.getTelefone());
            userDto.setSenha(dto.getSenha());
            userDto.setEndereco(dto.getEndereco());
            userDto.setRole(dto.getRole() != null ? dto.getRole() : Role.ROLE_CLIENTE);

            UsuarioModel usuarioSalvo = usuarioService.criarUsuario(userDto);

            // Criação da Conta
            ContaBancariaDto contaDto = new ContaBancariaDto();
            contaDto.setUsuarioId(usuarioSalvo.getId());
            
            // ✅ CORREÇÃO: Passando o Enum diretamente (sem o .name())
            contaDto.setTipoConta(dto.getTipoConta()); 
            
            ContaBancariaModel contaSalva = contaService.criarConta(contaDto);

            registrarProposta(dto, StatusProposta.APROVADA, "Onboarding concluído", 600);

            return new PropostaResponseDto(
                usuarioSalvo.getId(), 
                contaSalva.getId(), 
                "APROVADA", 
                "Conta aberta com sucesso!"
            );

        } catch (Exception e) {
            registrarProposta(dto, StatusProposta.NEGADA, e.getMessage(), 0);
            throw new RuntimeException("Erro no processamento: " + e.getMessage());
        }
    }

    private void registrarProposta(PropostaRequestDto dto, StatusProposta status, String obs, int score) {
        PropostaModel proposta = new PropostaModel(
            dto.getCpf(),
            dto.getNomeCompleto(),
            status,
            obs,
            score,
            LocalDateTime.now()
        );
        propostaRepository.save(proposta);
    }
}