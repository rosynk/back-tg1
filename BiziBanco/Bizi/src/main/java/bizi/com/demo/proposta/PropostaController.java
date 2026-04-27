package bizi.com.demo.proposta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Processo de adesão de novos clientes")
public class PropostaController {

    @Autowired
    private PropostaService propostaService;

    @PostMapping("/proposta")
    @Operation(summary = "Submeter nova proposta", description = "Recebe dados do cliente, valida CPF/Email e cria Usuário + Conta se aprovado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sucesso: Usuário e Conta criados"),
        @ApiResponse(responseCode = "400", description = "Erro: Dados inválidos ou CPF irregular"),
        @ApiResponse(responseCode = "409", description = "Conflito: Já existe uma proposta para este CPF")
    })
    public ResponseEntity<?> submeterProposta(@Valid @RequestBody PropostaRequestDto dto) {
        try {
            PropostaResponseDto resposta = propostaService.processarAbertura(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (RuntimeException e) {
            // Retorna a mensagem de erro específica (ex: "CPF Inválido") para o Front-end
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Classe auxiliar para o JSON de erro ficar limpo
    public static class ErrorResponse {
        private String mensagem;
        public ErrorResponse(String mensagem) { this.mensagem = mensagem; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    }
}