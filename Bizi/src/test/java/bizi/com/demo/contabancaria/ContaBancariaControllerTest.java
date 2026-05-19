package bizi.com.demo.contabancaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.contaBancaria.ContaBancariaController;
import bizi.com.demo.contaBancaria.ContaBancariaDto;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.contaBancaria.TipoConta;

@ExtendWith(MockitoExtension.class)
class ContaBancariaControllerTest {

    @Mock private ContaBancariaService contaBancariaService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ContaBancariaController controller;

    private ContaBancariaModel conta;

    @BeforeEach
    void setUp() {
        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setNumeroAgencia("0001");
        conta.setNumeroConta("123456");
        conta.setTipoConta(TipoConta.CORRENTE);
        conta.setSaldo(new BigDecimal("1000.00"));
    }

    // Helpers para configurar o SecurityContextHolder
    private void mockComoAdmin() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockComoCliente() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // =========================================================
    //  GET /api/contas — listarContas
    // =========================================================

    @Test
    @DisplayName("listarContas: ADMIN deve receber todas as contas")
    void listarContas_adminDeveReceberTodasAsContas() {
        mockComoAdmin();
        when(contaBancariaService.listarTodas()).thenReturn(List.of(conta, new ContaBancariaModel()));

        ResponseEntity<?> response = controller.listarContas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).hasSize(2);
        verify(contaBancariaService).listarTodas();
        verify(contaBancariaService, never()).buscarMinhasContas();
    }

    @Test
    @DisplayName("listarContas: CLIENTE deve receber apenas suas contas")
    void listarContas_clienteDeveReceberApenasContasProprias() {
        mockComoCliente();
        when(contaBancariaService.buscarMinhasContas()).thenReturn(List.of(conta));

        ResponseEntity<?> response = controller.listarContas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).hasSize(1);
        verify(contaBancariaService).buscarMinhasContas();
        verify(contaBancariaService, never()).listarTodas();
    }

    // =========================================================
    //  POST /api/contas — criarConta
    // =========================================================

    @Test
    @DisplayName("criarConta: deve retornar 201 quando conta criada com sucesso")
    void criarConta_deveRetornar201QuandoSucesso() {
        ContaBancariaDto dto = new ContaBancariaDto();
        when(contaBancariaService.criarConta(dto)).thenReturn(conta);

        ResponseEntity<?> response = controller.criarConta(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(conta);
    }

    @Test
    @DisplayName("criarConta: deve retornar 400 quando serviço lança exceção")
    void criarConta_deveRetornar400QuandoErro() {
        ContaBancariaDto dto = new ContaBancariaDto();
        when(contaBancariaService.criarConta(dto)).thenThrow(new RuntimeException("Usuário não encontrado."));

        ResponseEntity<?> response = controller.criarConta(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (ContaBancariaController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).isEqualTo("Usuário não encontrado.");
    }

    // =========================================================
    //  GET /api/contas/minhas-contas
    // =========================================================

    @Test
    @DisplayName("buscarMinhasContas: deve retornar 200 com lista de contas do cliente")
    void buscarMinhasContas_deveRetornar200ComListaDeContas() {
        when(contaBancariaService.buscarMinhasContas()).thenReturn(List.of(conta));

        ResponseEntity<List<ContaBancariaModel>> response = controller.buscarMinhasContas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    // =========================================================
    //  PATCH /api/contas/meu-deposito — autoDeposito
    // =========================================================

    @Test
    @DisplayName("autoDeposito: deve retornar 200 quando depósito realizado")
    void autoDeposito_deveRetornar200QuandoSucesso() {
        doNothing().when(contaBancariaService).realizarAutoDepositoLogado(any());

        ResponseEntity<?> response = controller.autoDepositoSemId(new BigDecimal("500.00"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (ContaBancariaController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("500");
    }

    @Test
    @DisplayName("autoDeposito: deve retornar 400 quando valor inválido")
    void autoDeposito_deveRetornar400QuandoValorInvalido() {
        doThrow(new RuntimeException("O valor deve ser maior que zero."))
                .when(contaBancariaService).realizarAutoDepositoLogado(any());

        ResponseEntity<?> response = controller.autoDepositoSemId(BigDecimal.ZERO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (ContaBancariaController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("maior que zero");
    }

    // =========================================================
    //  GET /api/contas/{id} — buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com conta quando encontrada")
    void buscarPorId_deveRetornar200QuandoEncontrada() {
        when(contaBancariaService.buscarPorId(10L)).thenReturn(conta);

        ResponseEntity<?> response = controller.buscarPorId(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(conta);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 404 quando conta não existe")
    void buscarPorId_deveRetornar404QuandoNaoEncontrada() {
        when(contaBancariaService.buscarPorId(999L))
                .thenThrow(new ContaBancariaNotFoundException("Conta não encontrada."));

        ResponseEntity<?> response = controller.buscarPorId(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var body = (ContaBancariaController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("inexistente");
    }

    // =========================================================
    //  GET /api/contas/buscar — buscarPorAgenciaEConta
    // =========================================================

    @Test
    @DisplayName("buscarPorAgenciaEConta: deve retornar 200 com dados públicos do titular")
    void buscarPorAgenciaEConta_deveRetornar200ComDadosPublicos() {
        var titular = new bizi.com.demo.usuario.UsuarioModel();
        titular.setNomeCompleto("Ana Lima");
        conta.setUsuario(titular);

        when(contaBancariaService.buscarPorAgenciaENumeroConta("0001", "123456"))
                .thenReturn(Optional.of(conta));

        ResponseEntity<?> response = controller.buscarPorAgenciaEConta("0001", "123456");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (java.util.Map<?, ?>) response.getBody();
        assertThat(body.get("nomeCompleto")).isEqualTo("Ana Lima");
        assertThat(body.get("numeroAgencia")).isEqualTo("0001");
        assertThat(body.get("numeroConta")).isEqualTo("123456");
        assertThat(body.get("tipoConta")).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("buscarPorAgenciaEConta: deve retornar 404 quando conta não encontrada")
    void buscarPorAgenciaEConta_deveRetornar404QuandoNaoEncontrada() {
        when(contaBancariaService.buscarPorAgenciaENumeroConta(anyString(), anyString()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.buscarPorAgenciaEConta("9999", "000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    //  PATCH /api/contas/{id}/deposito-administrativo — depositoAdmin
    // =========================================================

    @Test
    @DisplayName("depositoAdmin: deve retornar 200 quando depósito realizado com sucesso")
    void depositoAdmin_deveRetornar200QuandoSucesso() {
        doNothing().when(contaBancariaService).depositar(10L, new BigDecimal("1000.00"));

        ResponseEntity<?> response = controller.depositoAdmin(10L, new BigDecimal("1000.00"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (ContaBancariaController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("processado com sucesso");
    }

    @Test
    @DisplayName("depositoAdmin: deve retornar 400 quando depósito falha")
    void depositoAdmin_deveRetornar400QuandoFalha() {
        doThrow(new RuntimeException("O valor deve ser maior que zero."))
                .when(contaBancariaService).depositar(anyLong(), any());

        ResponseEntity<?> response = controller.depositoAdmin(10L, BigDecimal.ZERO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    //  DELETE /api/contas/{id} — deletarConta
    // =========================================================

    @Test
    @DisplayName("deletarConta: deve retornar 204 quando conta deletada com sucesso")
    void deletarConta_deveRetornar204QuandoSucesso() {
        doNothing().when(contaBancariaService).deletarConta(10L);

        ResponseEntity<Void> response = controller.deletarConta(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("deletarConta: deve retornar 404 quando conta não existe")
    void deletarConta_deveRetornar404QuandoNaoExiste() {
        doThrow(new ContaBancariaNotFoundException("Inexistente."))
                .when(contaBancariaService).deletarConta(999L);

        ResponseEntity<Void> response = controller.deletarConta(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}