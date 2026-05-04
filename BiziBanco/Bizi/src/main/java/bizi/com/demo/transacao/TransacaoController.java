package bizi.com.demo.transacao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transação", description = "Endpoints para gerenciamento de transações bancárias")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @Operation(summary = "Buscar transações por conta", description = "Retorna o histórico validando o CPF do titular.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @GetMapping("/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> buscarPorConta(@PathVariable Long idConta) {
        try {
            // Garante que o retorno seja a lista que a Service já filtrou
            List<TransacaoModel> transacoes = transacaoService.buscarPorConta(idConta);
            return ResponseEntity.ok(transacoes);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao buscar transações: " + e.getMessage()));
        }
    }

    @Operation(summary = "Consulta o extrato bancário", description = "Retorna o extrato limpo do usuário autenticado.")
    @GetMapping("/extrato")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> exibirExtrato() {
        try {
            // Esta chamada deve retornar a lista onde o campo 'detalhe' está preenchido
            List<TransacaoModel> extrato = transacaoService.listarExtratoCompleto();
            return ResponseEntity.ok(extrato);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao gerar extrato: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> criarTransacao(@Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            // A Service aqui deve salvar o campo 'detalhe' com o nome do favorecido/pagador
            TransacaoModel transacao = transacaoService.criarTransacao(transacaoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erro ao processar transação: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransacaoModel>> listarTodas() {
        return ResponseEntity.ok(transacaoService.listarTodas());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarTransacao(@PathVariable Long id) {
        try {
            transacaoService.deletarTransacao(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static class ErrorResponse {
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