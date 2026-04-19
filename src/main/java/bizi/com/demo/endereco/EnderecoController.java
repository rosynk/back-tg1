package bizi.com.demo.endereco;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/enderecos")
@Tag(name = "Endereço", description = "Gerenciamento de endereços com integração automática ViaCEP")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Criar endereço", description = "Cadastra um endereço. Envie apenas CEP, Número e Complemento.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "CEP inexistente ou dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Falha na comunicação com serviço externo (ViaCEP)")
    })
    public ResponseEntity<?> criarEndereco(@Valid @RequestBody EnderecoDto enderecoDto) {
        try {
            EnderecoModel endereco = enderecoService.criarEndereco(enderecoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(endereco);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Buscar por ID", description = "Retorna os detalhes de um endereço específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
        @ApiResponse(responseCode = "403", description = "Você não tem permissão para ver este endereço"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<?> buscarPorId(
            @Parameter(description = "ID do endereço", example = "1") @PathVariable Long id) {
        try {
            EnderecoModel endereco = enderecoService.buscarPorId(id);
            return ResponseEntity.ok(endereco);
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Acesso negado."));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Atualizar endereço", description = "Atualiza os dados. Se o CEP for novo, a rua e cidade serão corrigidas automaticamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou novo CEP não encontrado"),
        @ApiResponse(responseCode = "404", description = "ID de endereço não existe")
    })
    public ResponseEntity<?> atualizarEndereco(
            @Parameter(description = "ID do endereço", example = "1") @PathVariable Long id, 
            @Valid @RequestBody EnderecoDto enderecoDto) {
        try {
            EnderecoModel endereco = enderecoService.atualizarEndereco(id, enderecoDto);
            return ResponseEntity.ok(endereco);
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/consultar-cep/{cep}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Consultar ViaCEP", description = "Apenas consulta a API externa, sem salvar no banco de dados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do CEP retornados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Formato de CEP inválido")
    })
    public ResponseEntity<?> consultarViaCep(@PathVariable String cep) {
        try {
            return ResponseEntity.ok(enderecoService.consultarCepExterno(cep));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar endereço (Admin)", description = "Remove permanentemente um endereço do sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "ID não encontrado")
    })
    public ResponseEntity<?> deletarEndereco(@PathVariable Long id) {
        try {
            enderecoService.deletarEndereco(id);
            return ResponseEntity.noContent().build();
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints de Auditoria (Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos", description = "Visualização global para administradores.")
    public ResponseEntity<List<EnderecoModel>> listarTodos() {
        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    // Classe de erro padrão para as respostas 400 e 403
    static class ErrorResponse {
        private String mensagem;
        public ErrorResponse(String mensagem) { this.mensagem = mensagem; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    }
}