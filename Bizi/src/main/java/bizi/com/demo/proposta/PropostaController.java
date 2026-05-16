package bizi.com.demo.proposta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Processo de adesão de novos clientes e submissão de documentos")
public class PropostaController {

        @Autowired
        private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

        @Autowired
        private PropostaService propostaService;

        @Autowired
        private PropostaRepository propostaRepository;

        @PostMapping(value = "/proposta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Submeter nova proposta com documentos", description = "Recebe o objeto JSON 'dados' com as informações cadastrais e os arquivos físicos de imagem. "
                        +
                          "As imagens são persistidas no storage local e as referências salvas no banco de dados.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso", content = @Content(schema = @Schema(implementation = PropostaResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados ou idade insuficiente"),
                        @ApiResponse(responseCode = "409", description = "Conflito: Já existe uma proposta ativa ou conta para este CPF"),
                        @ApiResponse(responseCode = "500", description = "Falha crítica ao processar upload de arquivos")
        })

        public ResponseEntity<PropostaResponseDto> submeterProposta(
                        @RequestPart("dados") @Parameter(description = "Dados da proposta em formato JSON", schema = @Schema(implementation = PropostaRequestDto.class)) String dadosJson,

                        @RequestPart("selfie") MultipartFile selfie,
                        @RequestPart("rgFrente") MultipartFile rgFrente,
                        @RequestPart("rgVerso") MultipartFile rgVerso,
                        @RequestPart("comprovante") MultipartFile comprovante) throws Exception {

                // Faz o parse manual
                PropostaRequestDto dto = objectMapper.readValue(dadosJson, PropostaRequestDto.class);

                // O Service coordena o resto
                PropostaResponseDto resposta = propostaService.processarAbertura(dto, selfie, rgFrente, rgVerso,
                                comprovante);

                URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(resposta.getUsuarioId())
                                .toUri();

                return ResponseEntity.created(uri).body(resposta);
        }

        @GetMapping("/proposta/{id}")
        @Operation(summary = "Consultar status da proposta", description = "Permite ao cliente verificar o andamento da sua análise documental através do ID do usuário.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Proposta localizada"),
                        @ApiResponse(responseCode = "404", description = "Proposta não encontrada para o ID fornecido")
        })
        public ResponseEntity<PropostaModel> consultarProposta(@PathVariable Long id) {
                // Busca a proposta vinculada ao ID (ou implemente lógica por CPF se preferir)
                return propostaRepository.findById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }
}