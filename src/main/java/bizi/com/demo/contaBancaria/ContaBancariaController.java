package bizi.com.demo.contaBancaria;

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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Conta Bancária", description = "Endpoints para gerenciamento de contas bancárias")
public class ContaBancariaController {

    @Autowired
    private ContaBancariaService contaBancariaService;

    /**
     * Cria uma nova conta bancária
     */
    @PostMapping
    @Operation(summary = "Criar conta bancária", description = "Cadastra uma nova conta bancária no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ContaBancariaModel> criarConta(
            @Valid @RequestBody ContaBancariaDto contaBancariaDto) {
        try {
            ContaBancariaModel conta = contaBancariaService.criarConta(contaBancariaDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(conta);
        } catch (ContaBancariaConflictException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca uma conta pelo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID", description = "Retorna os dados da conta pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conta encontrada"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<ContaBancariaModel> buscarPorId(
            @Parameter(description = "ID da conta")
            @PathVariable Long id) {
        try {
            ContaBancariaModel conta = contaBancariaService.buscarPorId(id);
            return ResponseEntity.ok(conta);
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca contas por usuário
     */
    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Buscar contas por usuário", description = "Retorna todas as contas de um usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contas encontradas"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<ContaBancariaModel>> buscarPorUsuario(
            @Parameter(description = "ID do usuário")
            @PathVariable Long idUsuario) {
        List<ContaBancariaModel> contas = contaBancariaService.buscarPorUsuario(idUsuario);
        return ResponseEntity.ok(contas);
    }

    /**
     * Busca uma conta por agência
     */
    @GetMapping("/agencia/{numeroAgencia}")
    @Operation(summary = "Buscar contas por agência", description = "Retorna todas as contas de uma agência")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contas encontradas")
    })
    public ResponseEntity<List<ContaBancariaModel>> buscarPorAgencia(
            @Parameter(description = "Número da agência")
            @PathVariable String numeroAgencia) {
        List<ContaBancariaModel> contas = contaBancariaService.buscarPorAgencia(numeroAgencia);
        return ResponseEntity.ok(contas);
    }

    /**
     * Lista todas as contas
     */
    @GetMapping
    @Operation(summary = "Listar contas", description = "Retorna uma lista com todas as contas cadastradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso")
    })
    public ResponseEntity<List<ContaBancariaModel>> listarTodas() {
        List<ContaBancariaModel> contas = contaBancariaService.listarTodas();
        return ResponseEntity.ok(contas);
    }

    /**
     * Atualiza uma conta
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta", description = "Atualiza os dados de uma conta existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ContaBancariaModel> atualizarConta(
            @Parameter(description = "ID da conta")
            @PathVariable Long id,
            @Valid @RequestBody ContaBancariaDto contaBancariaDto) {
        try {
            ContaBancariaModel conta = contaBancariaService.atualizarConta(id, contaBancariaDto);
            return ResponseEntity.ok(conta);
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ContaBancariaConflictException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ativa ou desativa uma conta
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Alterar status da conta", description = "Ativa ou desativa uma conta bancária")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<ContaBancariaModel> alterarStatus(
            @Parameter(description = "ID da conta")
            @PathVariable Long id,
            @Parameter(description = "Novo status (true = ativa, false = inativa)")
            @RequestBody Boolean novoStatus) {
        try {
            ContaBancariaModel conta = contaBancariaService.alterarStatus(id, novoStatus);
            return ResponseEntity.ok(conta);
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deleta uma conta pelo ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar conta por ID", description = "Remove uma conta do sistema pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Conta deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<Void> deletarConta(
            @Parameter(description = "ID da conta")
            @PathVariable Long id) {
        try {
            contaBancariaService.deletarConta(id);
            return ResponseEntity.noContent().build();
        } catch (ContaBancariaNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}