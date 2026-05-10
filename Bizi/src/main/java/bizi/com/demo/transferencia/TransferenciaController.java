package bizi.com.demo.transferencia;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
@Tag(name = "Transferência", description = "Endpoints para operações financeiras e gestão de extratos")
public class TransferenciaController {

    @Autowired
    private TransferenciaService transferenciaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Realizar transferência", description = "Executa uma transferência entre contas e gera os registros de transação no extrato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transferência realizada com sucesso", content = @Content(schema = @Schema(implementation = TransferenciaDto.class))),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente, limite diário excedido ou dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Horário de TED não permitido ou falta de permissão"),
            @ApiResponse(responseCode = "404", description = "Conta de destino não encontrada")
    })
    public ResponseEntity<TransferenciaDto> realizarTransferencia(@Valid @RequestBody TransferenciaDto dto) {
        TransferenciaDto response = transferenciaService.realizarTransferencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Buscar transferência por ID", description = "Retorna os detalhes técnicos de uma transferência específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência localizada", content = @Content(schema = @Schema(implementation = TransferenciaModel.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao tentar visualizar dados de terceiros"),
            @ApiResponse(responseCode = "404", description = "ID não encontrado")
    })
    public ResponseEntity<TransferenciaModel> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transferenciaService.buscarPorId(id));
    }

    @GetMapping("/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Histórico completo da conta", description = "Lista todas as transferências (enviadas e recebidas) de uma conta específica.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferenciaModel.class))))
    public ResponseEntity<List<TransferenciaModel>> buscarTodasDaConta(@PathVariable Long idConta) {
        return ResponseEntity.ok(transferenciaService.buscarTodasDaConta(idConta));
    }

    @GetMapping("/exportar/pdf/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Exportar extrato em PDF", description = "Gera um documento PDF formatado com as cores de entrada (Verde) e saída (Rosa).")
    @ApiResponse(responseCode = "200", description = "Arquivo PDF gerado", content = @Content(mediaType = "application/pdf"))
    public ResponseEntity<byte[]> exportarPdf(@PathVariable Long idConta) {
        byte[] pdfData = transferenciaService.gerarPdfExtrato(idConta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("extrato_bizi_" + idConta + ".pdf")
                .build());

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/csv/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Exportar extrato em CSV", description = "Gera um arquivo CSV para importação em planilhas.")
    @ApiResponse(responseCode = "200", description = "Arquivo CSV gerado", content = @Content(mediaType = "text/csv"))
    public ResponseEntity<byte[]> exportarCsv(@PathVariable Long idConta) {
        byte[] csvData = transferenciaService.gerarCsvExtrato(idConta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("extrato_bizi_" + idConta + ".csv")
                .build());

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    @DeleteMapping("/estorno/{idTransferencia}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Realizar estorno (Admin)", description = "Reverte os saldos e marca a transação como estornada no sistema. Exclusivo para perfis administrativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estorno realizado e registros atualizados"),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem executar esta ação"),
            @ApiResponse(responseCode = "404", description = "Transferência não localizada para estorno")
    })
    public ResponseEntity<Void> estornar(@PathVariable Long idTransferencia) {
        transferenciaService.estornarTransferencia(idTransferencia);
        return ResponseEntity.noContent().build();
    }
}