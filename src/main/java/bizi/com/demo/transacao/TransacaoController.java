package bizi.com.demo.transacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * Cria uma nova transação
     */
    @PostMapping
    @Operation(summary = "Criar transação", description = "Registra uma nova transação bancária")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou conta não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TransacaoModel> criarTransacao(
            @Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoModel transacao = transacaoService.criarTransacao(transacaoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
        } catch (TransacaoConflictException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca uma transação pelo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Retorna os dados da transação pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transação encontrada"),
        @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<TransacaoModel> buscarPorId(
            @Parameter(description = "ID da transação")
            @PathVariable Long id) {
        try {
            TransacaoModel transacao = transacaoService.buscarPorId(id);
            return ResponseEntity.ok(transacao);
        } catch (TransacaoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca transações por conta bancária
     */
    @GetMapping("/conta/{idConta}")
    @Operation(summary = "Buscar transações por conta", description = "Retorna todas as transações de uma conta bancária")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transações encontradas"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<List<TransacaoModel>> buscarPorConta(
            @Parameter(description = "ID da conta bancária")
            @PathVariable Long idConta) {
        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(idConta);
        return ResponseEntity.ok(transacoes);
    }

    /**
     * Busca transações por tipo
     */
    @GetMapping("/tipo/{tipoTransacao}")
    @Operation(summary = "Buscar transações por tipo", description = "Retorna todas as transações de um tipo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transações encontradas")
    })
    public ResponseEntity<List<TransacaoModel>> buscarPorTipo(
            @Parameter(description = "Tipo da transação (DEPOSITO, SAQUE, TRANSFERENCIA, PIX)")
            @PathVariable String tipoTransacao) {
        List<TransacaoModel> transacoes = transacaoService.buscarPorTipo(tipoTransacao);
        return ResponseEntity.ok(transacoes);
    }

    /**
     * Busca transações por conta e tipo
     */
    @GetMapping("/conta/{idConta}/tipo/{tipoTransacao}")
    @Operation(summary = "Buscar transações por conta e tipo", description = "Retorna transações filtradas por conta e tipo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transações encontradas")
    })
    public ResponseEntity<List<TransacaoModel>> buscarPorContaETipo(
            @Parameter(description = "ID da conta bancária")
            @PathVariable Long idConta,
            @Parameter(description = "Tipo da transação")
            @PathVariable String tipoTransacao) {
        List<TransacaoModel> transacoes = transacaoService.buscarPorContaETipo(idConta, tipoTransacao);
        return ResponseEntity.ok(transacoes);
    }

    /**
     * Lista todas as transações
     */
    @GetMapping
    @Operation(summary = "Listar transações", description = "Retorna uma lista com todas as transações cadastradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso")
    })
    public ResponseEntity<List<TransacaoModel>> listarTodas() {
        List<TransacaoModel> transacoes = transacaoService.listarTodas();
        return ResponseEntity.ok(transacoes);
    }

    /**
     * Atualiza uma transação
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transação atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<TransacaoModel> atualizarTransacao(
            @Parameter(description = "ID da transação")
            @PathVariable Long id,
            @Valid @RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoModel transacao = transacaoService.atualizarTransacao(id, transacaoDto);
            return ResponseEntity.ok(transacao);
        } catch (TransacaoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (TransacaoConflictException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deleta uma transação pelo ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação por ID", description = "Remove uma transação do sistema pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transação deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<Void> deletarTransacao(
            @Parameter(description = "ID da transação")
            @PathVariable Long id) {
        try {
            transacaoService.deletarTransacao(id);
            return ResponseEntity.noContent().build();
        } catch (TransacaoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}