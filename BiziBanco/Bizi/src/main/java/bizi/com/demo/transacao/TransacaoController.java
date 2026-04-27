package bizi.com.demo.transacao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // IMPORTANTE
import org.springframework.security.access.prepost.PreAuthorize; // IMPORTANTE
import org.springframework.web.bind.annotation.*;

import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.proposta.PropostaController.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping
    @Operation(summary = "Criar transação", description = "Clientes criam em suas contas; Admins em qualquer conta.")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')") // Ambos podem tentar
    public ResponseEntity<?> criarTransacao(@Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoModel transacao = transacaoService.criarTransacao(transacaoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Dono vê a dele; Admin vê todas.")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transacaoService.buscarPorId(id));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (TransacaoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/conta/{idConta}")
    @Operation(summary = "Buscar transações por conta", description = "Retorna o histórico de transações de uma conta específica.")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> buscarPorConta(@PathVariable Long idConta) {
        try {
            // O Service deve receber o ID da conta (Ex: 3 ou 4)
            return ResponseEntity.ok(transacaoService.buscarPorConta(idConta));
        } catch (ContaBancariaNotFoundException e) {
            // Se a conta não existir, retorna 404 em vez de quebrar o sistema
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ErrorResponse("Erro: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ErrorResponse("Erro inesperado: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar todas (ADMIN APENAS)", description = "Retorna o histórico global do banco.")
    @PreAuthorize("hasRole('ADMIN')") // Marcos nem vê o resultado aqui
    public ResponseEntity<List<TransacaoModel>> listarTodas() {
        return ResponseEntity.ok(transacaoService.listarTodas());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação (ADMIN APENAS)", description = "Apenas para correções administrativas.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarTransacao(@PathVariable Long id, @Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            return ResponseEntity.ok(transacaoService.atualizarTransacao(id, transacaoDto));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação (ADMIN APENAS)", description = "Remoção física de registro.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarTransacao(@PathVariable Long id) {
        try {
            transacaoService.deletarTransacao(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}