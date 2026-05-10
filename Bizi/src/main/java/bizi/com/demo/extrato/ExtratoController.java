package bizi.com.demo.extrato;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/extrato")
@Tag(name = "Extrato", description = "Gerenciamento de histórico financeiro e consolidação de transações")
@CrossOrigin(origins = "http://localhost:4200")
public class ExtratoController {

    @Autowired
    private ExtratoService extratoService;

    // --- MÉTODOS PARA O CLIENTE ---

    @Operation(summary = "Obter extrato por período (Cliente)", description = "Retorna o extrato consolidado da conta do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou ausente"),
            @ApiResponse(responseCode = "500", description = "Erro ao processar as transações")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> obterExtrato(
            @Parameter(description = "Data inicial (yyyy-MM-dd)") 
            @RequestParam(name = "inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final (yyyy-MM-dd)") 
            @RequestParam(name = "fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            return ResponseEntity.ok(extratoService.gerarExtrato(inicio, fim));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " + e.getMessage());
        }
    }

    @Operation(summary = "Exportar PDF por período", description = "Gera um arquivo PDF com cores diferenciadas para débitos e créditos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "500", description = "Erro na geração do documento")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(name = "inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            byte[] pdf = extratoService.gerarExtratoPdf(inicio, fim);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("extrato_bizi_" + inicio + "_a_" + fim + ".pdf").build());

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- MÉTODOS PARA O ADMINISTRADOR ---

    @Operation(summary = "[ADM] Consultar extrato completo", description = "Visualização administrativa de todos os lançamentos de uma conta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso na consulta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não localizada")
    })
    @GetMapping("/admin/{idConta}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> obterExtratoPorId(@PathVariable(name = "idConta") Long idConta) {
        try {
            return ResponseEntity.ok(extratoService.gerarExtratoParaAdm(idConta));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "[ADM] Consultar extrato com filtro", description = "Permite ao administrador auditar transações de qualquer conta em um período específico.")
    @GetMapping("/admin/{idConta}/filtro")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> obterExtratoPorIdEPeriodo(
            @PathVariable(name = "idConta") Long idConta,
            @RequestParam(name = "inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            return ResponseEntity.ok(extratoService.gerarExtratoParaAdmComPeriodo(idConta, inicio, fim));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}