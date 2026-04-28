package bizi.com.demo.transacao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.usuario.UsuarioModel;
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
            @ApiResponse(responseCode = "403", description = "Acesso negado: CPF não confere"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/conta/{idConta}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> buscarPorConta(@PathVariable Long idConta) {
        try {
            return ResponseEntity.ok(transacaoService.buscarPorConta(idConta));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao buscar transações: " + e.getMessage()));
        }
    }

    @Operation(summary = "Criar transação", description = "Cria depósito, saque ou transferência validando a posse da conta via CPF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transação criada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Usuário não autorizado a transacionar nesta conta"),
            @ApiResponse(responseCode = "400", description = "Dados da transação inválidos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> criarTransacao(@Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoModel transacao = transacaoService.criarTransacao(transacaoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Erro ao processar transação"));
        }
    }

    @Operation(summary = "Consulta o extrato bancário", description = "Retorna todas as transações vinculadas ao CPF do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato recuperado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Permissão insuficiente"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar a consulta")
    })
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/extrato")
    public ResponseEntity<?> exibirExtrato() {
        try {
            // Chamamos o método da Service que já cuida de pegar o CPF do usuário
            // autenticado
            List<TransacaoModel> extrato = transacaoService.listarExtratoCompleto();
            return ResponseEntity.ok(extrato);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao gerar extrato: " + e.getMessage()));
        }
    }

    @Operation(summary = "Listar todas (ADMIN)", description = "Retorna o log global de todas as transações do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista global recuperada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Requer ROLE_ADMIN")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransacaoModel>> listarTodas() {
        return ResponseEntity.ok(transacaoService.listarTodas());
    }

    @Operation(summary = "Deletar transação", description = "Remoção física de um registro de transação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação removida"),
            @ApiResponse(responseCode = "403", description = "Somente administradores podem deletar registros")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarTransacao(@PathVariable Long id) {
        try {
            transacaoService.deletarTransacao(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Classe auxiliar interna para padronizar erros JSON
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