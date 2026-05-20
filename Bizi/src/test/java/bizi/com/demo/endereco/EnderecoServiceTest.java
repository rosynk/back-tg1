package bizi.com.demo.endereco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.validacoes.external.ViaCepClient;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private EnderecoService service;

    private EnderecoModel endereco;
    private ViaCepClient.ViaCepResponse viaCepOk;

    @BeforeEach
    void setUp() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        endereco = new EnderecoModel();
        endereco.setId(1L);
        endereco.setCep("01310100");
        endereco.setRua("Avenida Paulista");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setNumero(1000);
        endereco.setComplemento("Apto 42");

        viaCepOk = new ViaCepClient.ViaCepResponse();
        viaCepOk.setLogradouro("Avenida Paulista");
        viaCepOk.setBairro("Bela Vista");
        viaCepOk.setLocalidade("São Paulo");
        viaCepOk.setUf("SP");
        viaCepOk.setErro(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    //  consultarCepExterno
    // =========================================================

    @Test
    @DisplayName("consultarCepExterno: deve retornar dados quando CEP válido")
    void consultarCepExterno_deveRetornarDadosQuandoCepValido() {
        when(viaCepClient.buscarEnderecoPorCep("01310100")).thenReturn(viaCepOk);

        ViaCepClient.ViaCepResponse resultado = service.consultarCepExterno("01310100");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getLogradouro()).isEqualTo("Avenida Paulista");
        assertThat(resultado.getLocalidade()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("consultarCepExterno: deve lançar exceção quando ViaCEP retorna erro")
    void consultarCepExterno_deveLancarExcecaoQuandoViaCepRetornaErro() {
        ViaCepClient.ViaCepResponse erroResponse = new ViaCepClient.ViaCepResponse();
        erroResponse.setErro(true);

        when(viaCepClient.buscarEnderecoPorCep("00000000")).thenReturn(erroResponse);

        assertThatThrownBy(() -> service.consultarCepExterno("00000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CEP não encontrado");
    }

    @Test
    @DisplayName("consultarCepExterno: deve lançar exceção quando ViaCEP retorna null")
    void consultarCepExterno_deveLancarExcecaoQuandoViaCepRetornaNull() {
        when(viaCepClient.buscarEnderecoPorCep("99999999")).thenReturn(null);

        assertThatThrownBy(() -> service.consultarCepExterno("99999999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CEP não encontrado");
    }

    // =========================================================
    //  criarEndereco
    // =========================================================

    @Test
    @DisplayName("criarEndereco: deve preencher dados do ViaCEP e salvar")
    void criarEndereco_devePreencherCamposDoViaCepESalvar() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");
        dto.setNumero(1000);
        dto.setComplemento("Apto 42");

        when(viaCepClient.buscarEnderecoPorCep("01310100")).thenReturn(viaCepOk);
        when(enderecoRepository.save(any(EnderecoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        EnderecoModel resultado = service.criarEndereco(dto);

        assertThat(resultado.getCep()).isEqualTo("01310100");
        assertThat(resultado.getRua()).isEqualTo("Avenida Paulista");
        assertThat(resultado.getBairro()).isEqualTo("Bela Vista");
        assertThat(resultado.getCidade()).isEqualTo("São Paulo");
        assertThat(resultado.getEstado()).isEqualTo("SP");
        assertThat(resultado.getNumero()).isEqualTo(1000);
        assertThat(resultado.getComplemento()).isEqualTo("Apto 42");

        verify(enderecoRepository).save(any(EnderecoModel.class));
    }

    @Test
    @DisplayName("criarEndereco: deve lançar exceção quando CEP inválido")
    void criarEndereco_deveLancarExcecaoQuandoCepInvalido() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("00000000");

        ViaCepClient.ViaCepResponse erro = new ViaCepClient.ViaCepResponse();
        erro.setErro(true);

        when(viaCepClient.buscarEnderecoPorCep("00000000")).thenReturn(erro);

        assertThatThrownBy(() -> service.criarEndereco(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CEP não encontrado");

        verify(enderecoRepository, never()).save(any());
    }

    // =========================================================
    //  buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar endereço quando existe")
    void buscarPorId_deveRetornarEnderecoQuandoExiste() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));

        EnderecoModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(endereco);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar EnderecoNotFoundException quando não encontrado")
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        when(enderecoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EnderecoNotFoundException.class)
                .hasMessageContaining("999");
    }

    // =========================================================
    //  atualizarEndereco
    // =========================================================

    @Test
    @DisplayName("atualizarEndereco: deve atualizar sem consultar ViaCEP quando CEP é igual")
    void atualizarEndereco_deveAtualizarSemViaCepQuandoCepIgual() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");
        dto.setNumero(2000);
        dto.setComplemento("Bloco B");

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));
        when(enderecoRepository.save(endereco)).thenReturn(endereco);

        EnderecoModel resultado = service.atualizarEndereco(1L, dto);

        assertThat(resultado.getCep()).isEqualTo("01310100");
        assertThat(resultado.getNumero()).isEqualTo(2000);
        assertThat(resultado.getComplemento()).isEqualTo("Bloco B");

        verify(viaCepClient, never()).buscarEnderecoPorCep(any());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("atualizarEndereco: deve consultar ViaCEP quando CEP muda")
    void atualizarEndereco_deveConsultarViaCepQuandoCepMuda() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01001000");
        dto.setNumero(500);
        dto.setComplemento("Casa");

        ViaCepClient.ViaCepResponse novoCep = new ViaCepClient.ViaCepResponse();
        novoCep.setLogradouro("Praça da Sé");
        novoCep.setBairro("Sé");
        novoCep.setLocalidade("São Paulo");
        novoCep.setUf("SP");
        novoCep.setErro(false);

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));
        when(viaCepClient.buscarEnderecoPorCep("01001000")).thenReturn(novoCep);
        when(enderecoRepository.save(endereco)).thenReturn(endereco);

        EnderecoModel resultado = service.atualizarEndereco(1L, dto);

        assertThat(resultado.getCep()).isEqualTo("01001000");
        assertThat(resultado.getRua()).isEqualTo("Praça da Sé");
        assertThat(resultado.getBairro()).isEqualTo("Sé");
        assertThat(resultado.getCidade()).isEqualTo("São Paulo");
        assertThat(resultado.getEstado()).isEqualTo("SP");
        assertThat(resultado.getNumero()).isEqualTo(500);
        assertThat(resultado.getComplemento()).isEqualTo("Casa");

        verify(viaCepClient).buscarEnderecoPorCep("01001000");
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("atualizarEndereco: deve lançar exceção quando endereço não encontrado")
    void atualizarEndereco_deveLancarExcecaoQuandoEnderecoNaoEncontrado() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");

        when(enderecoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarEndereco(999L, dto))
                .isInstanceOf(EnderecoNotFoundException.class);

        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizarEndereco: deve lançar exceção quando novo CEP é inválido")
    void atualizarEndereco_deveLancarExcecaoQuandoNovoCepInvalido() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("00000000");

        ViaCepClient.ViaCepResponse erro = new ViaCepClient.ViaCepResponse();
        erro.setErro(true);

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));
        when(viaCepClient.buscarEnderecoPorCep("00000000")).thenReturn(erro);

        assertThatThrownBy(() -> service.atualizarEndereco(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CEP não encontrado");

        verify(enderecoRepository, never()).save(any());
    }

    // =========================================================
    //  deletarEndereco
    // =========================================================

    @Test
    @DisplayName("deletarEndereco: deve deletar quando endereço existe")
    void deletarEndereco_deveDeletarQuandoEnderecoExiste() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));

        service.deletarEndereco(1L);

        verify(enderecoRepository).delete(endereco);
    }

    @Test
    @DisplayName("deletarEndereco: deve lançar exceção quando endereço não existe")
    void deletarEndereco_deveLancarExcecaoQuandoNaoExiste() {
        when(enderecoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletarEndereco(999L))
                .isInstanceOf(EnderecoNotFoundException.class)
                .hasMessageContaining("inexistente");

        verify(enderecoRepository, never()).delete(any());
    }

    // =========================================================
    //  listarTodos / buscarPorCep / buscarPorCidadeEEstado
    // =========================================================

    @Test
    @DisplayName("listarTodos: deve retornar todos os endereços")
    void listarTodos_deveRetornarTodosOsEnderecos() {
        when(enderecoRepository.findAll()).thenReturn(List.of(endereco, new EnderecoModel()));

        List<EnderecoModel> resultado = service.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("buscarPorCep: deve retornar lista de endereços com o CEP informado")
    void buscarPorCep_deveRetornarListaComCep() {
        when(enderecoRepository.findByCep("01310100")).thenReturn(List.of(endereco));

        List<EnderecoModel> resultado = service.buscarPorCep("01310100");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCep()).isEqualTo("01310100");
    }

    @Test
    @DisplayName("buscarPorCidadeEEstado: deve retornar endereços filtrados por cidade e estado")
    void buscarPorCidadeEEstado_deveRetornarEnderecosFiltrados() {
        when(enderecoRepository.findByCidadeAndEstado("São Paulo", "SP")).thenReturn(List.of(endereco));

        List<EnderecoModel> resultado = service.buscarPorCidadeEEstado("São Paulo", "SP");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCidade()).isEqualTo("São Paulo");
        assertThat(resultado.get(0).getEstado()).isEqualTo("SP");
    }

    @Test
    @DisplayName("buscarPorCidadeEEstado: deve retornar lista vazia quando não há resultados")
    void buscarPorCidadeEEstado_deveRetornarVazioQuandoSemResultados() {
        when(enderecoRepository.findByCidadeAndEstado("Cidade Fantasma", "XX")).thenReturn(List.of());

        List<EnderecoModel> resultado = service.buscarPorCidadeEEstado("Cidade Fantasma", "XX");

        assertThat(resultado).isEmpty();
    }
}