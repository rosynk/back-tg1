package bizi.com.demo.proposta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class PropostaControllerTest {

    @Mock private PropostaService propostaService;
    @Mock private PropostaRepository propostaRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private PropostaController controller;

    private PropostaModel propostaModel;
    private PropostaResponseDto propostaResponseDto;
    private PropostaRequestDto propostaRequestDto;
    private MultipartFile selfie;
    private MultipartFile rgFrente;
    private MultipartFile rgVerso;
    private MultipartFile comprovante;

    @BeforeEach
    void setUp() {
        propostaModel = new PropostaModel();
        propostaModel.setId(1L);
        propostaModel.setCpf("11144477735");
        propostaModel.setNomeCompleto("João Silva");
        propostaModel.setStatus(StatusProposta.PENDENTE);

        propostaResponseDto = new PropostaResponseDto(1L, 10L, "PENDENTE", "PROPOSTA RECEBIDA!", "/files/selfie.jpg");

        propostaRequestDto = new PropostaRequestDto();
        propostaRequestDto.setCpf("11144477735");
        propostaRequestDto.setNomeCompleto("João Silva");
        propostaRequestDto.setEmail("joao@email.com");

        selfie      = new MockMultipartFile("selfie",      "selfie.jpg",      "image/jpeg", new byte[]{1});
        rgFrente    = new MockMultipartFile("rgFrente",    "rgFrente.jpg",    "image/jpeg", new byte[]{2});
        rgVerso     = new MockMultipartFile("rgVerso",     "rgVerso.jpg",     "image/jpeg", new byte[]{3});
        comprovante = new MockMultipartFile("comprovante", "comprovante.jpg", "image/jpeg", new byte[]{4});
    }

    // =========================================================
    //  POST /api/onboarding/proposta — submeterProposta
    // =========================================================

    @Test
    @DisplayName("submeterProposta: deve propagar exceção quando serviço lança RuntimeException")
    void submeterProposta_devePropararExcecaoQuandoServicoFalha() throws Exception {
        when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(propostaRequestDto);
        when(propostaService.processarAbertura(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("CPF inválido."));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                controller.submeterProposta("{}", selfie, rgFrente, rgVerso, comprovante))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF inválido");
    }

    // =========================================================
    //  GET /api/onboarding/proposta/{id} — consultarProposta
    // =========================================================

    @Test
    @DisplayName("consultarProposta: deve retornar 200 com proposta quando encontrada")
    void consultarProposta_deveRetornar200QuandoEncontrada() {
        when(propostaRepository.findById(1L)).thenReturn(Optional.of(propostaModel));

        ResponseEntity<PropostaModel> response = controller.consultarProposta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(propostaModel);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusProposta.PENDENTE);
    }

    @Test
    @DisplayName("consultarProposta: deve retornar 404 quando proposta não encontrada")
    void consultarProposta_deveRetornar404QuandoNaoEncontrada() {
        when(propostaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<PropostaModel> response = controller.consultarProposta(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

@ExtendWith(MockitoExtension.class)
class PropostaAdmControllerTest {

    @Mock private PropostaService propostaService;
    @Mock private PropostaRepository propostaRepository;

    @InjectMocks
    private PropostaAdmController controller;

    private PropostaModel propostaModel;

    @BeforeEach
    void setUp() {
        propostaModel = new PropostaModel();
        propostaModel.setId(1L);
        propostaModel.setCpf("11144477735");
        propostaModel.setNomeCompleto("João Silva");
        propostaModel.setStatus(StatusProposta.PENDENTE);
    }

    // =========================================================
    //  GET /api/propostas — listarTodas
    // =========================================================

    @Test
    @DisplayName("listarTodas: deve retornar 200 com todas as propostas")
    void listarTodas_deveRetornar200ComTodasAsPropostas() {
        PropostaModel proposta2 = new PropostaModel();
        proposta2.setId(2L);
        proposta2.setStatus(StatusProposta.APROVADA);

        when(propostaRepository.findAll()).thenReturn(List.of(propostaModel, proposta2));

        ResponseEntity<List<PropostaModel>> response = controller.listarTodas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("listarTodas: deve retornar 200 com lista vazia quando não há propostas")
    void listarTodas_deveRetornar200ComListaVazia() {
        when(propostaRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<PropostaModel>> response = controller.listarTodas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    //  PUT /api/propostas/{id}/avaliar — avaliar
    // =========================================================

    @Test
    @DisplayName("avaliar: deve retornar 200 com proposta APROVADA")
    void avaliar_deveRetornar200ComPropostaAprovada() {
        propostaModel.setStatus(StatusProposta.APROVADA);
        propostaModel.setObservacao("Documentos validados");

        AvaliacaoRequest req = new AvaliacaoRequest(StatusProposta.APROVADA, "Documentos validados");
        when(propostaService.avaliarProposta(1L, StatusProposta.APROVADA, "Documentos validados"))
                .thenReturn(propostaModel);

        ResponseEntity<PropostaModel> response = controller.avaliar(1L, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusProposta.APROVADA);
        assertThat(response.getBody().getObservacao()).isEqualTo("Documentos validados");
    }

    @Test
    @DisplayName("avaliar: deve retornar 200 com proposta NEGADA")
    void avaliar_deveRetornar200ComPropostaNegada() {
        propostaModel.setStatus(StatusProposta.NEGADA);
        propostaModel.setObservacao("Documento ilegível");

        AvaliacaoRequest req = new AvaliacaoRequest(StatusProposta.NEGADA, "Documento ilegível");
        when(propostaService.avaliarProposta(1L, StatusProposta.NEGADA, "Documento ilegível"))
                .thenReturn(propostaModel);

        ResponseEntity<PropostaModel> response = controller.avaliar(1L, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusProposta.NEGADA);
    }

    @Test
    @DisplayName("avaliar: deve propagar exceção quando proposta não encontrada")
    void avaliar_devePropararExcecaoQuandoPropostaNaoEncontrada() {
        AvaliacaoRequest req = new AvaliacaoRequest(StatusProposta.APROVADA, "obs");
        when(propostaService.avaliarProposta(999L, StatusProposta.APROVADA, "obs"))
                .thenThrow(new RuntimeException("Proposta não encontrada com o ID: 999"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.avaliar(999L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }
}