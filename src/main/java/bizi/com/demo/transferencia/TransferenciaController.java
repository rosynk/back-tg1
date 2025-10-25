package bizi.com.demo.transferencia;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transferencias")
@Tag(name = "Transferência", description = "Endpoints para transferências bancárias entre contas")
public class TransferenciaController {

    @Autowired
    private TransferenciaService transferenciaService;

    /**
     * Realiza uma transferência bancária
     */
    @PostMapping
    @Operation(summary = "Realizar transferência", 
               description = "Realiza uma transferência bancária entre contas seguindo regras do BACEN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transferência realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, saldo insuficiente ou limite excedido"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> realizarTransferencia(
            @Valid @RequestBody TransferenciaDto transferenciaDto) {
        try {
            TransferenciaDto response = transferenciaService.realizarTransferencia(transferenciaDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (TransferenciaException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar transferência: " + e.getMessage()));
        }
    }

    /**
     * Busca uma transferência pelo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar transferência por ID", 
               description = "Retorna os dados de uma transferência pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transferência encontrada"),
        @ApiResponse(responseCode = "404", description = "Transferência não encontrada")
    })
    public ResponseEntity<TransferenciaModel> buscarPorId(
            @Parameter(description = "ID da transferência")
            @PathVariable Long id) {
        try {
            TransferenciaModel transferencia = transferenciaService.buscarPorId(id);
            return ResponseEntity.ok(transferencia);
        } catch (TransferenciaNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca transferências enviadas por uma conta
     */
    @GetMapping("/enviadas/conta/{idConta}")
    @Operation(summary = "Buscar transferências enviadas", 
               description = "Retorna todas as transferências enviadas por uma conta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transferências encontradas")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarEnviadas(
            @Parameter(description = "ID da conta de origem")
            @PathVariable Long idConta) {
        List<TransferenciaModel> transferencias = transferenciaService.buscarPorContaOrigem(idConta);
        return ResponseEntity.ok(transferencias);
    }

    /**
     * Busca transferências recebidas por uma conta
     */
    @GetMapping("/recebidas/conta/{idConta}")
    @Operation(summary = "Buscar transferências recebidas", 
               description = "Retorna todas as transferências recebidas por uma conta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transferências encontradas")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarRecebidas(
            @Parameter(description = "ID da conta de destino")
            @PathVariable Long idConta) {
        List<TransferenciaModel> transferencias = transferenciaService.buscarPorContaDestino(idConta);
        return ResponseEntity.ok(transferencias);
    }

    /**
     * Busca todas as transferências de uma conta (enviadas e recebidas)
     */
    @GetMapping("/conta/{idConta}")
    @Operation(summary = "Buscar todas as transferências da conta", 
               description = "Retorna todas as transferências (enviadas e recebidas) de uma conta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transferências encontradas")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarTodasDaConta(
            @Parameter(description = "ID da conta")
            @PathVariable Long idConta) {
        List<TransferenciaModel> transferencias = transferenciaService.buscarTodasDaConta(idConta);
        return ResponseEntity.ok(transferencias);
    }

    /**
     * Classe para resposta de erro
     */
    static class ErrorResponse {
        private String mensagem;

        public ErrorResponse(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }
    }
}