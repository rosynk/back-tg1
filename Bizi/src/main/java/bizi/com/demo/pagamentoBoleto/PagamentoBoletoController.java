package bizi.com.demo.pagamentoBoleto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagamentos/boleto")
@Tag(name = "Pagamento Boleto", description = "Endpoints para pagamento de boletos bancários")
public class PagamentoBoletoController {

    @Autowired
    private PagamentoBoletoService pagamentoBoletoService;

    @PostMapping
    @Operation(summary = "Realizar pagamento de boleto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pagamento realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou saldo insuficiente"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> realizarPagamento(@Valid @RequestBody PagamentoBoletoDto dto) {
        try {
        	PagamentoBoletoModel response = pagamentoBoletoService.realizarPagamento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (PagamentoBoletoException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar pagamento: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<PagamentoBoletoDto> buscarPorId( @PathVariable Long id) {
    	
    	PagamentoBoletoDto dto = pagamentoBoletoService.buscarPorId(id);
        try {
        	return ResponseEntity.ok(dto);
        } catch (PagamentoBoletoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/conta/{idConta}")
    @Operation(summary = "Buscar pagamentos por conta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamentos encontrados")
    })
    public ResponseEntity<List<PagamentoBoletoDto>> buscarPorConta(
            @Parameter(description = "ID da conta") @PathVariable Long idConta) {
        return ResponseEntity.ok(pagamentoBoletoService.buscarPorConta(idConta));
    }

    static class ErrorResponse {
        private String mensagem;
        public ErrorResponse(String mensagem) { this.mensagem = mensagem; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    }
}