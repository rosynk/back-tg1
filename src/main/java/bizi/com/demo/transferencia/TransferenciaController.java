package bizi.com.demo.transferencia;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transferencias")
@Tag(name = "Transferência", description = "Endpoints para operações financeiras entre contas")
public class TransferenciaController {

    @Autowired
    private TransferenciaService transferenciaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Realizar transferência", description = "Envia valores para uma conta destino. Clientes usam a própria conta; Admins especificam origem.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201", description = "Transferência realizada com sucesso", 
            content = @Content(schema = @Schema(implementation = TransferenciaDto.class))),
        @ApiResponse(responseCode = "400", description = "Saldo insuficiente, dados inválidos ou fora do horário permitido"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado (Token inválido ou ausente)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para o recurso solicitado"),
        @ApiResponse(responseCode = "404", description = "Conta de origem ou destino não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno ao processar a transação ou mapear a resposta")
    })
    public ResponseEntity<TransferenciaDto> realizarTransferencia(
            @Valid @RequestBody TransferenciaDto transferenciaDto) {
        TransferenciaDto response = transferenciaService.realizarTransferencia(transferenciaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Buscar por ID", description = "Retorna os detalhes de uma transferência específica.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Transferência encontrada",
            content = @Content(schema = @Schema(implementation = TransferenciaModel.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para ver esta transferência"),
        @ApiResponse(responseCode = "404", description = "ID da transferência não encontrado")
    })
    public ResponseEntity<TransferenciaModel> buscarPorId(@PathVariable Long id) {
        TransferenciaModel transferencia = transferenciaService.buscarPorId(id);
        return ResponseEntity.ok(transferencia);
    }

    @GetMapping("/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Histórico completo da conta", description = "Lista todas as entradas e saídas de uma conta específica.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Extrato gerado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Proibido acessar extrato de contas de terceiros"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarTodasDaConta(@PathVariable Long idConta) {
        List<TransferenciaModel> transferencias = transferenciaService.buscarTodasDaConta(idConta);
        return ResponseEntity.ok(transferencias);
    }

    @GetMapping("/enviadas/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Filtrar apenas enviadas", description = "Lista as transferências enviadas por uma conta específica.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Lista de transferências enviadas retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (Cliente só pode ver a própria conta)")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarEnviadas(@PathVariable Long idConta) {
        return ResponseEntity.ok(transferenciaService.buscarPorContaOrigem(idConta));
    }

    @GetMapping("/recebidas/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Filtrar apenas recebidas", description = "Lista as transferências recebidas por uma conta específica.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Lista de transferências recebidas retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (Cliente só pode ver a própria conta)")
    })
    public ResponseEntity<List<TransferenciaModel>> buscarRecebidas(@PathVariable Long idConta) {
        return ResponseEntity.ok(transferenciaService.buscarPorContaDestino(idConta));
    }

    @GetMapping("/exportar/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Exportar para CSV", description = "Gera um arquivo CSV com o histórico de transações da conta.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "CSV gerado com sucesso", 
            content = @Content(mediaType = "text/csv")),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Proibido exportar dados de terceiros"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<byte[]> exportarExtrato(@PathVariable Long idConta) {
        byte[] csvData = transferenciaService.gerarCsvExtrato(idConta);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato_bizi_conta_" + idConta + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}