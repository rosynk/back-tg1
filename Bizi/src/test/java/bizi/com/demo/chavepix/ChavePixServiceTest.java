package bizi.com.demo.chavepix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.chavePix.ChavePixRepository;
import bizi.com.demo.chavePix.ChavePixService;
import bizi.com.demo.chavePix.TipoChave;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class) // Ativa os mocks do Mockito sem subir o Spring
class ChavePixServiceTest {

    // --- Mocks das dependências ---
    @Mock private ChavePixRepository repository;
    @Mock private ContaBancariaService contaService;
    @Mock private ContaBancariaRepository contaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    // Injeta os mocks acima no service que vamos testar
    @InjectMocks
    private ChavePixService service;

    // Objetos reutilizados nos testes
    private UsuarioModel usuario;
    private ContaBancariaModel conta;

    @BeforeEach
    void setUp() {
        // Monta um usuário e uma conta fictícios antes de cada teste
        conta = new ContaBancariaModel();
        conta.setId(1L);

        usuario = new UsuarioModel();
        usuario.setCpf("12345678900");
        usuario.setEmail("teste@bizi.com");
        usuario.setTelefone("11999999999");
        usuario.setContas(List.of(conta));
    }

    // =========================================================
    //  cadastrarChave — casos de sucesso
    // =========================================================

    @Test
    @DisplayName("Deve cadastrar chave CPF com sucesso")
    void deveCadastrarChaveCpfComSucesso() {
        when(repository.countByContaBancariaId(1L)).thenReturn(0L);
        when(repository.existsByContaBancariaIdAndValor(1L, usuario.getCpf())).thenReturn(false);
        when(contaService.buscarPorId(1L)).thenReturn(conta);

        ChavePixModel chaveEsperada = new ChavePixModel();
        chaveEsperada.setValor(usuario.getCpf());
        when(repository.save(any(ChavePixModel.class))).thenReturn(chaveEsperada);

        ChavePixModel resultado = service.cadastrarChave(1L, TipoChave.CPF, usuario);

        assertThat(resultado.getValor()).isEqualTo(usuario.getCpf());
        verify(repository).save(any(ChavePixModel.class));
    }

    @Test
    @DisplayName("Deve cadastrar chave EMAIL com sucesso")
    void deveCadastrarChaveEmailComSucesso() {
        when(repository.countByContaBancariaId(1L)).thenReturn(1L);
        when(repository.existsByContaBancariaIdAndValor(1L, usuario.getEmail())).thenReturn(false);
        when(contaService.buscarPorId(1L)).thenReturn(conta);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChavePixModel resultado = service.cadastrarChave(1L, TipoChave.EMAIL, usuario);

        assertThat(resultado.getValor()).isEqualTo(usuario.getEmail());
        assertThat(resultado.getTipoChave()).isEqualTo("EMAIL");
    }

    @Test
    @DisplayName("Deve cadastrar chave TELEFONE com sucesso")
    void deveCadastrarChaveTelefoneComSucesso() {
        when(repository.countByContaBancariaId(1L)).thenReturn(0L);
        when(repository.existsByContaBancariaIdAndValor(1L, usuario.getTelefone())).thenReturn(false);
        when(contaService.buscarPorId(1L)).thenReturn(conta);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChavePixModel resultado = service.cadastrarChave(1L, TipoChave.TELEFONE, usuario);

        assertThat(resultado.getValor()).isEqualTo(usuario.getTelefone());
    }

    @Test
    @DisplayName("Deve cadastrar chave ALEATORIA com UUID gerado")
    void deveCadastrarChaveAleatoriaComUUID() {
        when(repository.countByContaBancariaId(1L)).thenReturn(0L);
        when(repository.existsByContaBancariaIdAndValor(eq(1L), anyString())).thenReturn(false);
        when(contaService.buscarPorId(1L)).thenReturn(conta);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChavePixModel resultado = service.cadastrarChave(1L, TipoChave.ALEATORIA, usuario);

        // UUID tem 36 caracteres (ex: "550e8400-e29b-41d4-a716-446655440000")
        assertThat(resultado.getValor()).hasSize(36);
        assertThat(resultado.getTipoChave()).isEqualTo("ALEATORIA");
    }

    // =========================================================
    //  cadastrarChave — casos de erro
    // =========================================================

    @Test
    @DisplayName("Deve lançar exceção ao atingir limite de 5 chaves")
    void deveLancarExcecaoLimiteChaves() {
        when(repository.countByContaBancariaId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> service.cadastrarChave(1L, TipoChave.CPF, usuario))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Limite de 5 chaves");

        // Garante que nunca salvou nada
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar chave duplicada")
    void deveLancarExcecaoChaveDuplicada() {
        when(repository.countByContaBancariaId(1L)).thenReturn(2L);
        when(repository.existsByContaBancariaIdAndValor(1L, usuario.getCpf())).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarChave(1L, TipoChave.CPF, usuario))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("já possui uma chave CPF");

        verify(repository, never()).save(any());
    }

    // =========================================================
    //  buscarPorValor
    // =========================================================

    @Test
    @DisplayName("Deve retornar chave quando valor existe")
    void deveRetornarChaveQuandoValorExiste() {
        ChavePixModel chave = new ChavePixModel();
        chave.setValor("12345678900");
        when(repository.findByValor("12345678900")).thenReturn(Optional.of(chave));

        Optional<ChavePixModel> resultado = service.buscarPorValor("12345678900");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getValor()).isEqualTo("12345678900");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando valor não existe")
    void deveRetornarVazioQuandoValorNaoExiste() {
        when(repository.findByValor("chaveinexistente")).thenReturn(Optional.empty());

        Optional<ChavePixModel> resultado = service.buscarPorValor("chaveinexistente");

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    //  listarChavesPorConta
    // =========================================================

    @Test
    @DisplayName("Deve retornar lista de chaves da conta")
    void deveRetornarListaDeChavesDaConta() {
        ChavePixModel c1 = new ChavePixModel();
        ChavePixModel c2 = new ChavePixModel();
        when(repository.findByContaBancariaId(1L)).thenReturn(List.of(c1, c2));

        List<ChavePixModel> resultado = service.listarChavesPorConta(1L);

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando conta não tem chaves")
    void deveRetornarListaVaziaQuandoSemChaves() {
        when(repository.findByContaBancariaId(1L)).thenReturn(new ArrayList<>());

        List<ChavePixModel> resultado = service.listarChavesPorConta(1L);

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    //  removerChave
    // =========================================================

    @Test
    @DisplayName("Deve chamar deleteById ao remover chave")
    void deveChamarDeleteByIdAoRemover() {
        service.removerChave(42L);

        verify(repository).deleteById(42L);
    }

    // =========================================================
    //  buscarIdContaPorUsuario
    // =========================================================

    @Test
    @DisplayName("Deve retornar ID da conta quando usuário existe e tem conta")
    void deveRetornarIdDaContaComSucesso() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        Long contaId = service.buscarIdContaPorUsuario("12345678900");

        assertThat(contaId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF não encontrado")
    void deveLancarExcecaoCpfNaoEncontrado() {
        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarIdContaPorUsuario("00000000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário existe mas não tem conta")
    void deveLancarExcecaoUsuarioSemConta() {
        UsuarioModel usuarioSemConta = new UsuarioModel();
        usuarioSemConta.setCpf("99999999999");
        usuarioSemConta.setContas(new ArrayList<>()); // lista vazia

        when(usuarioRepository.findByCpf("99999999999")).thenReturn(Optional.of(usuarioSemConta));

        assertThatThrownBy(() -> service.buscarIdContaPorUsuario("99999999999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não possui nenhuma conta");
    }

    // =========================================================
    //  buscarDetalhesDaConta
    // =========================================================

    @Test
    @DisplayName("Deve retornar conta quando CPF tem conta vinculada")
    void deveRetornarDetalhesDaContaComSucesso() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        ContaBancariaModel resultado = service.buscarDetalhesDaConta("12345678900");

        assertThat(resultado).isEqualTo(conta);
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não existe no repositório")
    void deveLancarExcecaoContaNaoExisteNoRepositorio() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));
        when(contaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarDetalhesDaConta("12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");
    }
}