package bizi.com.demo.proposta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import bizi.com.demo.comunicacao.ComunicacaoService;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.infra.storage.Disco;
import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;
import bizi.com.demo.usuario.UsuarioService;
import bizi.com.demo.validacoes.CPF.CPFValidador;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    @Mock private ComunicacaoService comunicacaoService;
    @Mock private UsuarioService usuarioService;
    @Mock private ContaBancariaService contaService;
    @Mock private PropostaRepository propostaRepository;
    @Mock private CPFValidador cpfValidador;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Disco disco;

    @InjectMocks
    private PropostaService propostaService;

    private PropostaRequestDto dto;
    private UsuarioModel usuario;
    private ContaBancariaModel conta;
    private MultipartFile selfie;
    private MultipartFile rgFrente;
    private MultipartFile rgVerso;
    private MultipartFile comprovante;

    @BeforeEach
    void setUp() {
        dto = new PropostaRequestDto();
        dto.setNomeCompleto("João Silva");
        dto.setCpf("11144477735");
        dto.setEmail("joao@email.com");
        dto.setTelefone("11999999999");
        dto.setSenha("senha123");
        dto.setDataNascimento(LocalDate.of(1990, 1, 1)); // maior de idade
        dto.setRole(Role.ROLE_CLIENTE);

        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setCpf("11144477735");
        usuario.setEmail("joao@email.com");
        usuario.setNomeCompleto("João Silva");
        usuario.setAtivo(false);

        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setStatusConta(false);

        selfie      = new MockMultipartFile("selfie",      "selfie.jpg",      "image/jpeg", new byte[]{1});
        rgFrente    = new MockMultipartFile("rgFrente",    "rgFrente.jpg",    "image/jpeg", new byte[]{2});
        rgVerso     = new MockMultipartFile("rgVerso",     "rgVerso.jpg",     "image/jpeg", new byte[]{3});
        comprovante = new MockMultipartFile("comprovante", "comprovante.jpg", "image/jpeg", new byte[]{4});
    }

    // =========================================================
    //  processarAbertura — fluxo feliz
    // =========================================================

    @Test
    @DisplayName("processarAbertura: deve criar usuário, conta, registrar proposta PENDENTE e enviar e-mail de boas-vindas")
    void processarAbertura_deveCriarUsuarioContaEPropostaPendente() throws Exception {
        when(cpfValidador.isValid(dto.getCpf())).thenReturn(true);
        when(disco.salvar(selfie)).thenReturn("/files/selfie.jpg");
        when(disco.salvar(rgFrente)).thenReturn("/files/rgFrente.jpg");
        when(disco.salvar(rgVerso)).thenReturn("/files/rgVerso.jpg");
        when(disco.salvar(comprovante)).thenReturn("/files/comprovante.jpg");
        when(usuarioService.criarUsuario(any())).thenReturn(usuario);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.criarConta(any())).thenReturn(conta);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PropostaResponseDto resposta = propostaService.processarAbertura(
                dto, selfie, rgFrente, rgVerso, comprovante);

        assertThat(resposta.getUsuarioId()).isEqualTo(1L);
        assertThat(resposta.getContaId()).isEqualTo(10L);
        assertThat(resposta.getStatus()).isEqualTo("PENDENTE");
        assertThat(resposta.getUrlSelfieConfirmada()).isEqualTo("/files/selfie.jpg");
        verify(comunicacaoService).enviarEmailBoasVindas(dto.getEmail(), dto.getNomeCompleto());
    }

    @Test
    @DisplayName("processarAbertura: deve forçar usuário como inativo após criação")
    void processarAbertura_deveForcarUsuarioComoInativo() throws Exception {
        when(cpfValidador.isValid(dto.getCpf())).thenReturn(true);
        when(disco.salvar(any())).thenReturn("/files/arquivo.jpg");
        when(usuarioService.criarUsuario(any())).thenReturn(usuario);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.criarConta(any())).thenReturn(conta);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante);

        assertThat(usuario.isAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("processarAbertura: deve salvar os 4 arquivos no disco")
    void processarAbertura_deveSalvarOsQuatroArquivosNoDisco() throws Exception {
        when(cpfValidador.isValid(dto.getCpf())).thenReturn(true);
        when(disco.salvar(any())).thenReturn("/files/arquivo.jpg");
        when(usuarioService.criarUsuario(any())).thenReturn(usuario);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.criarConta(any())).thenReturn(conta);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante);

        verify(disco).salvar(selfie);
        verify(disco).salvar(rgFrente);
        verify(disco).salvar(rgVerso);
        verify(disco).salvar(comprovante);
    }

    @Test
    @DisplayName("processarAbertura: deve usar ROLE_CLIENTE como padrão quando dto não informa role")
    void processarAbertura_deveUsarRoleClienteComoDefault() throws Exception {
        dto.setRole(null);

        when(cpfValidador.isValid(dto.getCpf())).thenReturn(true);
        when(disco.salvar(any())).thenReturn("/files/arquivo.jpg");
        when(usuarioService.criarUsuario(any())).thenAnswer(inv -> {
            bizi.com.demo.usuario.UsuarioDto userDto = inv.getArgument(0);
            assertThat(userDto.getRole()).isEqualTo(Role.ROLE_CLIENTE);
            return usuario;
        });
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.criarConta(any())).thenReturn(conta);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante);

        verify(usuarioService).criarUsuario(any());
    }

    // =========================================================
    //  processarAbertura — validações de negócio
    // =========================================================

    @Test
    @DisplayName("processarAbertura: deve lançar exceção e registrar proposta NEGADA para menor de 18 anos")
    void processarAbertura_deveLancarExcecaoParaMenorDeIdade() throws Exception {
        dto.setDataNascimento(LocalDate.now().minusYears(17));

        assertThatThrownBy(() ->
                propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("menores de 18");

        verify(propostaRepository).save(any()); // proposta NEGADA registrada
        verify(usuarioService, never()).criarUsuario(any());
        verify(disco, never()).salvar(any());
    }

    @Test
    @DisplayName("processarAbertura: deve lançar exceção e registrar proposta NEGADA para CPF inválido")
    void processarAbertura_deveLancarExcecaoParaCpfInvalido() throws Exception {
        dto.setDataNascimento(LocalDate.of(1990, 1, 1)); // maior de idade
        when(cpfValidador.isValid(dto.getCpf())).thenReturn(false);

        assertThatThrownBy(() ->
                propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF informado não é válido");

        verify(propostaRepository).save(any()); // proposta NEGADA registrada
        verify(usuarioService, never()).criarUsuario(any());
        verify(disco, never()).salvar(any());
    }

    @Test
    @DisplayName("processarAbertura: deve registrar proposta NEGADA e relançar exceção quando criação do usuário falha")
    void processarAbertura_deveRegistrarNegadaQuandoCriacaoUsuarioFalha() throws Exception {
        when(cpfValidador.isValid(dto.getCpf())).thenReturn(true);
        when(disco.salvar(any())).thenReturn("/files/arquivo.jpg");
        when(usuarioService.criarUsuario(any())).thenThrow(new RuntimeException("CPF já cadastrado."));
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() ->
                propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso, comprovante))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao processar onboarding");

        // Duas saves: uma do catch (NEGADA), total de 2 chamadas ao repository
        verify(propostaRepository, org.mockito.Mockito.atLeastOnce()).save(any());
        verify(comunicacaoService, never()).enviarEmailBoasVindas(anyString(), anyString());
    }

    // =========================================================
    //  avaliarProposta — APROVADA
    // =========================================================

    @Test
    @DisplayName("avaliarProposta: deve ativar usuário e conta quando aprovada")
    void avaliarProposta_deveAtivarUsuarioEContaQuandoAprovada() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");
        proposta.setUrlSelfie("/files/selfie.jpg");
        proposta.setUrlRgFrente("/files/rg.jpg");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.buscarPorUsuario(usuario.getId())).thenReturn(conta);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PropostaModel resultado = propostaService.avaliarProposta(1L, StatusProposta.APROVADA, "Tudo certo");

        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.getUrlSelfie()).isEqualTo("/files/selfie.jpg");
        assertThat(usuario.getUrlRgFrente()).isEqualTo("/files/rg.jpg");
        assertThat(conta.getStatusConta()).isTrue();
        assertThat(resultado.getStatus()).isEqualTo(StatusProposta.APROVADA);
        assertThat(resultado.getObservacao()).isEqualTo("Tudo certo");
        verify(comunicacaoService).enviarEmailAprovacao(usuario.getEmail(), usuario.getNomeCompleto());
    }

    @Test
    @DisplayName("avaliarProposta: deve aprovar mesmo quando conta não encontrada (conta null)")
    void avaliarProposta_deveAprovarMesmoSemConta() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(contaService.buscarPorUsuario(usuario.getId())).thenReturn(null);
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PropostaModel resultado = propostaService.avaliarProposta(1L, StatusProposta.APROVADA, "Aprovado");

        assertThat(resultado.getStatus()).isEqualTo(StatusProposta.APROVADA);
        verify(contaService, never()).atualizarConta(any());
    }

    @Test
    @DisplayName("avaliarProposta: deve lançar exceção quando proposta não encontrada")
    void avaliarProposta_deveLancarExcecaoQuandoPropostaNaoEncontrada() {
        when(propostaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                propostaService.avaliarProposta(999L, StatusProposta.APROVADA, "obs"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("avaliarProposta: deve lançar exceção quando usuário não encontrado ao aprovar")
    void avaliarProposta_deveLancarExcecaoQuandoUsuarioNaoEncontradoAoAprovar() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                propostaService.avaliarProposta(1L, StatusProposta.APROVADA, "obs"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // =========================================================
    //  avaliarProposta — NEGADA
    // =========================================================

    @Test
    @DisplayName("avaliarProposta: deve excluir arquivos do disco quando negada")
    void avaliarProposta_deveExcluirArquivosQuandoNegada() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");
        proposta.setUrlSelfie("/files/selfie.jpg");
        proposta.setUrlRgFrente("/files/rg.jpg");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.avaliarProposta(1L, StatusProposta.NEGADA, "Documento ilegível");

        verify(disco).excluir("/files/selfie.jpg");
        verify(disco).excluir("/files/rg.jpg");
    }

    @Test
    @DisplayName("avaliarProposta: deve limpar URLs de selfie e RG quando negada")
    void avaliarProposta_deveLimparUrlsQuandoNegada() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");
        proposta.setUrlSelfie("/files/selfie.jpg");
        proposta.setUrlRgFrente("/files/rg.jpg");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PropostaModel resultado = propostaService.avaliarProposta(1L, StatusProposta.NEGADA, "Reprovado");

        assertThat(resultado.getUrlSelfie()).isNull();
        assertThat(resultado.getUrlRgFrente()).isNull();
    }

    @Test
    @DisplayName("avaliarProposta: deve enviar e-mail de negação com observação")
    void avaliarProposta_deveEnviarEmailDeNegacao() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");
        proposta.setUrlSelfie("/files/selfie.jpg");
        proposta.setUrlRgFrente("/files/rg.jpg");

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.avaliarProposta(1L, StatusProposta.NEGADA, "Documento ilegível");

        verify(comunicacaoService).enviarEmailNegacao(
                eq(usuario.getEmail()),
                eq(usuario.getNomeCompleto()),
                eq("Documento ilegível"));
    }

    @Test
    @DisplayName("avaliarProposta: não deve excluir arquivos quando URLs são nulas")
    void avaliarProposta_naoDeveExcluirArquivosQuandoUrlsNulas() {
        PropostaModel proposta = new PropostaModel();
        proposta.setId(1L);
        proposta.setCpf("11144477735");
        proposta.setUrlSelfie(null);
        proposta.setUrlRgFrente(null);

        when(propostaRepository.findById(1L)).thenReturn(Optional.of(proposta));
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuario));
        when(propostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        propostaService.avaliarProposta(1L, StatusProposta.NEGADA, "Dados inválidos");

        verify(disco, never()).excluir(any());
    }
}