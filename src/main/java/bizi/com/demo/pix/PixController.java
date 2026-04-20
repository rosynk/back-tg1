package bizi.com.demo.pix;

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
@RequestMapping("/api/pix")
@Tag(name = "Pix", description = "Endpoints para transações Pix")
public class PixController {

    @Autowired
    private PixService pixService;

    @PostMapping
    @Operation(summary = "Realizar Pix",
               description = "Realiza uma transação Pix 24/7 seguindo regras do BACEN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pix realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, saldo insuficiente ou limite excedido"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> realizarPix(@Valid @RequestBody PixDto pixDto) {
        try {
            PixDto response = pixService.realizarPix(pixDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (PixException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar Pix: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Pix por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pix encontrado"),
        @ApiResponse(responseCode = "404", description = "Pix não encontrado")
    })
    public ResponseEntity<PixModel> buscarPorId(
            @Parameter(description = "ID do Pix") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(pixService.buscarPorId(id));
        } catch (PixNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/enviados/conta/{idConta}")
    @Operation(summary = "Buscar Pix enviados por uma conta")
    public ResponseEntity<List<PixModel>> buscarEnviados(
            @Parameter(description = "ID da conta de origem") @PathVariable Long idConta) {
        return ResponseEntity.ok(pixService.buscarEnviados(idConta));
    }

    @GetMapping("/recebidos/conta/{idConta}")
    @Operation(summary = "Buscar Pix recebidos por uma conta")
    public ResponseEntity<List<PixModel>> buscarRecebidos(
            @Parameter(description = "ID da conta de destino") @PathVariable Long idConta) {
        return ResponseEntity.ok(pixService.buscarRecebidos(idConta));
    }

    @GetMapping("/conta/{idConta}")
    @Operation(summary = "Buscar todos os Pix de uma conta (enviados e recebidos)")
    public ResponseEntity<List<PixModel>> buscarTodosDaConta(
            @Parameter(description = "ID da conta") @PathVariable Long idConta) {
        return ResponseEntity.ok(pixService.buscarTodosDaConta(idConta));
    }

    static class ErrorResponse {
        private String mensagem;
        public ErrorResponse(String mensagem) { this.mensagem = mensagem; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    }
}