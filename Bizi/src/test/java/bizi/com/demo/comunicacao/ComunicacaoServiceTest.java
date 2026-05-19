package bizi.com.demo.comunicacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComunicacaoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ComunicacaoService service;

    // Captura o SimpleMailMessage que foi passado pro mailSender.send(...)
    private ArgumentCaptor<SimpleMailMessage> captor;

    @BeforeEach
    void setUp() {
        captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    }

    // =========================================================
    //  enviarEmailRecuperacao
    // =========================================================

    @Test
    @DisplayName("Recuperação: deve enviar e-mail real quando mailSender está disponível")
    void recuperacao_deveEnviarEmailRealQuandoMailSenderDisponivel() {
        service.enviarEmailRecuperacao("usuario@teste.com", "123456");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getTo()).containsExactly("usuario@teste.com");
        assertThat(msg.getSubject()).isEqualTo("Bizi - Código de Recuperação");
        assertThat(msg.getText()).contains("123456");
    }

    @Test
    @DisplayName("Recuperação: deve cair no fallback (console) quando mailSender é nulo")
    void recuperacao_deveCairNoFallbackQuandoMailSenderNulo() {
        // Substitui o mailSender injetado por null, simulando ambiente sem e-mail configurado
        ReflectionTestUtils.setField(service, "mailSender", null);

        // Não deve lançar nenhuma exceção — o fallback imprime no console
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailRecuperacao("usuario@teste.com", "654321")
        );

        // Garante que send() nunca foi chamado
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Recuperação: deve cair no fallback quando mailSender lança exceção")
    void recuperacao_deveCairNoFallbackQuandoMailSenderFalha() {
        doThrow(new RuntimeException("Falha SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        // Não deve propagar a exceção — o service trata internamente
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailRecuperacao("usuario@teste.com", "999999")
        );
    }

    // =========================================================
    //  enviarEmailBoasVindas
    // =========================================================

    @Test
    @DisplayName("Boas-vindas: deve enviar e-mail real com assunto e conteúdo corretos")
    void boasVindas_deveEnviarEmailRealComConteudoCorreto() {
        service.enviarEmailBoasVindas("joao@teste.com", "João Silva");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getTo()).containsExactly("joao@teste.com");
        assertThat(msg.getSubject()).contains("proposta foi recebida");
        assertThat(msg.getText()).contains("João Silva");
        assertThat(msg.getText()).contains("2 dias úteis");
    }

    @Test
    @DisplayName("Boas-vindas: deve cair no fallback quando mailSender é nulo")
    void boasVindas_deveCairNoFallbackQuandoMailSenderNulo() {
        ReflectionTestUtils.setField(service, "mailSender", null);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailBoasVindas("joao@teste.com", "João Silva")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Boas-vindas: deve cair no fallback quando mailSender lança exceção")
    void boasVindas_deveCairNoFallbackQuandoMailSenderFalha() {
        doThrow(new RuntimeException("Falha SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailBoasVindas("joao@teste.com", "João Silva")
        );
    }

    // =========================================================
    //  enviarEmailAprovacao
    // =========================================================

    @Test
    @DisplayName("Aprovação: deve enviar e-mail com link de acesso")
    void aprovacao_deveEnviarEmailComLinkDeAcesso() {
        service.enviarEmailAprovacao("maria@teste.com", "Maria Souza");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getTo()).containsExactly("maria@teste.com");
        assertThat(msg.getSubject()).contains("aprovada");
        assertThat(msg.getText()).contains("Maria Souza");
        assertThat(msg.getText()).contains("localhost:4200/login");
    }

    @Test
    @DisplayName("Aprovação: deve cair no fallback quando mailSender é nulo")
    void aprovacao_deveCairNoFallbackQuandoMailSenderNulo() {
        ReflectionTestUtils.setField(service, "mailSender", null);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailAprovacao("maria@teste.com", "Maria Souza")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Aprovação: deve cair no fallback quando mailSender lança exceção")
    void aprovacao_deveCairNoFallbackQuandoMailSenderFalha() {
        doThrow(new RuntimeException("Timeout SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailAprovacao("maria@teste.com", "Maria Souza")
        );
    }

    // =========================================================
    //  enviarEmailNegacao
    // =========================================================

    @Test
    @DisplayName("Negação: deve enviar e-mail com motivo informado")
    void negacao_deveEnviarEmailComMotivoInformado() {
        service.enviarEmailNegacao("carlos@teste.com", "Carlos Lima", "Documentos inválidos");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getTo()).containsExactly("carlos@teste.com");
        assertThat(msg.getSubject()).contains("não aprovada");
        assertThat(msg.getText()).contains("Carlos Lima");
        assertThat(msg.getText()).contains("Documentos inválidos");
        assertThat(msg.getText()).contains("LGPD");
    }

    @Test
    @DisplayName("Negação: deve exibir 'Não informado' quando motivo é nulo")
    void negacao_deveExibirNaoInformadoQuandoMotivoNulo() {
        service.enviarEmailNegacao("carlos@teste.com", "Carlos Lima", null);

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getText()).contains("Não informado");
    }

    @Test
    @DisplayName("Negação: deve exibir 'Não informado' quando motivo é string vazia")
    void negacao_deveExibirNaoInformadoQuandoMotivoVazio() {
        service.enviarEmailNegacao("carlos@teste.com", "Carlos Lima", "   ");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getText()).contains("Não informado");
    }

    @Test
    @DisplayName("Negação: deve cair no fallback quando mailSender é nulo")
    void negacao_deveCairNoFallbackQuandoMailSenderNulo() {
        ReflectionTestUtils.setField(service, "mailSender", null);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailNegacao("carlos@teste.com", "Carlos Lima", "Motivo X")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Negação: deve cair no fallback quando mailSender lança exceção")
    void negacao_deveCairNoFallbackQuandoMailSenderFalha() {
        doThrow(new RuntimeException("Erro SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> service.enviarEmailNegacao("carlos@teste.com", "Carlos Lima", "Motivo X")
        );
    }
}