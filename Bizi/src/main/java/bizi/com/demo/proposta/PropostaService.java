package bizi.com.demo.proposta;

import bizi.com.demo.comunicacao.ComunicacaoService;
import bizi.com.demo.contaBancaria.ContaBancariaDto;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.infra.storage.Disco;
import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioDto;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;
import bizi.com.demo.usuario.UsuarioService;
import bizi.com.demo.validacoes.CPF.CPFValidador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
public class PropostaService {
	@Autowired
	private ComunicacaoService comunicacaoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private CPFValidador cpfValidador;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private Disco disco; // Interface para salvar localmente ou no Drive

    @Transactional
    public PropostaResponseDto processarAbertura(PropostaRequestDto dto, MultipartFile selfie, MultipartFile rgFrente,
            MultipartFile rgVerso, MultipartFile comprovante) {

        // 1. Validação de Idade
        int idade = Period.between(dto.getDataNascimento(), LocalDate.now()).getYears();
        if (idade < 18) {
            // Adicionados os 4 nulls correspondentes às URLs de documentos
            registrarProposta(dto, StatusProposta.NEGADA, "Usuário menor de idade", 0, null, null, null, null);
            throw new RuntimeException("Não é permitido abrir conta para menores de 18 anos.");
        }

        // 2. Validação de CPF
        if (!cpfValidador.isValid(dto.getCpf())) {
            // Adicionados os 4 nulls correspondentes às URLs de documentos
            registrarProposta(dto, StatusProposta.NEGADA, "CPF Inválido", 0, null, null, null, null);
            throw new RuntimeException("O CPF informado não é válido.");
        }

        try {
            // 3. Persistência Física dos Arquivos (Disco Local ou Drive)
            String caminhoSelfie = disco.salvar(selfie);
            String caminhoRg = disco.salvar(rgFrente);
            String caminhoRgVerso = disco.salvar(rgVerso);
            String caminhoComprovante = disco.salvar(comprovante);

            // 4. Criação do Usuário (Inicia desativado até aprovação do ADM)
            UsuarioDto userDto = new UsuarioDto();
            userDto.setNomeCompleto(dto.getNomeCompleto());
            userDto.setCpf(dto.getCpf());
            userDto.setEmail(dto.getEmail());
            userDto.setTelefone(dto.getTelefone());
            userDto.setSenha(dto.getSenha());
            userDto.setEndereco(dto.getEndereco());
            userDto.setRole(dto.getRole() != null ? dto.getRole() : Role.ROLE_CLIENTE);

            UsuarioModel usuarioSalvo = usuarioService.criarUsuario(userDto);

            // Forçamos o status inativo inicialmente
            usuarioSalvo.setAtivo(false);
            usuarioRepository.save(usuarioSalvo);

            // 5. Criação da Conta Bancária vinculada
            ContaBancariaDto contaDto = new ContaBancariaDto();
            contaDto.setUsuarioId(usuarioSalvo.getId());
            contaDto.setTipoConta(dto.getTipoConta());

            ContaBancariaModel contaSalva = contaService.criarConta(contaDto);

            // 6. Registro da Proposta com os caminhos dos arquivos
            registrarProposta(dto, StatusProposta.PENDENTE, "Aguardando validação", 600,
                    caminhoSelfie, caminhoRg, caminhoRgVerso, caminhoComprovante);

            comunicacaoService.enviarEmailBoasVindas(dto.getEmail(), dto.getNomeCompleto());

            return new PropostaResponseDto(
                    usuarioSalvo.getId(),
                    contaSalva.getId(),
                    "PENDENTE",
                    "PROPOSTA RECEBIDA!",
                    caminhoSelfie);

        } catch (Exception e) {
            // Se der erro, registrar como negada
            registrarProposta(dto, StatusProposta.NEGADA, "Erro: " + e.getMessage(), 0, null, null, null, null);
            throw new RuntimeException("Erro ao processar onboarding: " + e.getMessage());
        }
    }

    private void registrarProposta(PropostaRequestDto dto, StatusProposta status, String obs, int score,
            String urlSelfie, String urlRgF, String urlRgV, String urlComp) {
        PropostaModel proposta = new PropostaModel(
                dto.getCpf(),
                dto.getNomeCompleto(),
                status,
                obs,
                score,
                LocalDateTime.now(),
                urlRgF,
                urlRgV,
                urlComp,
                urlSelfie);
        propostaRepository.save(proposta);
    }
    
  
    
    
    @Transactional
    public PropostaModel avaliarProposta(Long id, StatusProposta novoStatus, String observacao) {
        PropostaModel proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada com o ID: " + id));

        if (novoStatus == StatusProposta.APROVADA) {
            UsuarioModel usuario = usuarioRepository.findByCpf(proposta.getCpf())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado para ativação"));

            // 1. Ativa o usuário
            usuario.setAtivo(true);
            usuario.setUrlSelfie(proposta.getUrlSelfie());
            usuario.setUrlRgFrente(proposta.getUrlRgFrente());
            usuarioRepository.save(usuario);

            // 2. ATIVAÇÃO DA CONTA (CORRIGIDO)
            // Usamos o buscarPorUsuario, pois o ID do usuário NÃO é o mesmo ID da conta
            ContaBancariaModel conta = contaService.buscarPorUsuario(usuario.getId()); 
            
            if (conta != null) {
                conta.setStatusConta(true); 
                contaService.atualizarConta(conta); 
                System.out.println("Conta bancária ativada com sucesso para: " + usuario.getNomeCompleto());
            } else {
                System.err.println("Aviso: Conta não encontrada para o usuário " + usuario.getId());
            }

        } else if (novoStatus == StatusProposta.NEGADA) {
            // Lógica LGPD: Exclusão de arquivos
            if (proposta.getUrlSelfie() != null) disco.excluir(proposta.getUrlSelfie());
            if (proposta.getUrlRgFrente() != null) disco.excluir(proposta.getUrlRgFrente());
            
            proposta.setUrlSelfie(null);
            proposta.setUrlRgFrente(null);
        }

        proposta.setStatus(novoStatus);
        proposta.setObservacao(observacao);
        return propostaRepository.save(proposta);
    }
    
    
    
    

}