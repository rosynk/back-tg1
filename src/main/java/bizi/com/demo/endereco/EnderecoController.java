package bizi.com.demo.endereco;

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
@RequestMapping("/api/enderecos")
@Tag(name = "Endereço", description = "Endpoints para gerenciamento de endereços")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    /**
     * Cria um novo endereço
     */
    @PostMapping
    @Operation(summary = "Criar endereço", description = "Cadastra um novo endereço no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<EnderecoModel> criarEndereco(
            @Valid @RequestBody EnderecoDto enderecoDto) {
        try {
            EnderecoModel endereco = enderecoService.criarEndereco(enderecoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(endereco);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca um endereço pelo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID", description = "Retorna os dados do endereço pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<EnderecoModel> buscarPorId(
            @Parameter(description = "ID do endereço")
            @PathVariable Long id) {
        try {
            EnderecoModel endereco = enderecoService.buscarPorId(id);
            return ResponseEntity.ok(endereco);
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todos os endereços
     */
    @GetMapping
    @Operation(summary = "Listar endereços", description = "Retorna uma lista com todos os endereços cadastrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso")
    })
    public ResponseEntity<List<EnderecoModel>> listarTodos() {
        List<EnderecoModel> enderecos = enderecoService.listarTodos();
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Busca endereços por CEP
     */
    @GetMapping("/cep/{cep}")
    @Operation(summary = "Buscar endereços por CEP", description = "Retorna endereços com o CEP informado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços encontrados"),
        @ApiResponse(responseCode = "400", description = "CEP inválido")
    })
    public ResponseEntity<List<EnderecoModel>> buscarPorCep(
            @Parameter(description = "CEP do endereço (apenas números)")
            @PathVariable String cep) {
        List<EnderecoModel> enderecos = enderecoService.buscarPorCep(cep);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Busca endereços por cidade
     */
    @GetMapping("/cidade/{cidade}")
    @Operation(summary = "Buscar endereços por cidade", description = "Retorna endereços na cidade informada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços encontrados")
    })
    public ResponseEntity<List<EnderecoModel>> buscarPorCidade(
            @Parameter(description = "Nome da cidade")
            @PathVariable String cidade) {
        List<EnderecoModel> enderecos = enderecoService.buscarPorCidade(cidade);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Busca endereços por estado
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar endereços por estado", description = "Retorna endereços no estado informado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços encontrados")
    })
    public ResponseEntity<List<EnderecoModel>> buscarPorEstado(
            @Parameter(description = "Sigla do estado")
            @PathVariable String estado) {
        List<EnderecoModel> enderecos = enderecoService.buscarPorEstado(estado);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Busca endereços por bairro
     */
    @GetMapping("/bairro/{bairro}")
    @Operation(summary = "Buscar endereços por bairro", description = "Retorna endereços no bairro informado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços encontrados")
    })
    public ResponseEntity<List<EnderecoModel>> buscarPorBairro(
            @Parameter(description = "Nome do bairro")
            @PathVariable String bairro) {
        List<EnderecoModel> enderecos = enderecoService.buscarPorBairro(bairro);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Busca endereços por cidade e estado
     */
    @GetMapping("/cidade-estado")
    @Operation(summary = "Buscar endereços por cidade e estado", description = "Retorna endereços na cidade e estado informados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços encontrados")
    })
    public ResponseEntity<List<EnderecoModel>> buscarPorCidadeEEstado(
            @Parameter(description = "Nome da cidade")
            @RequestParam String cidade,
            @Parameter(description = "Sigla do estado")
            @RequestParam String estado) {
        List<EnderecoModel> enderecos = enderecoService.buscarPorCidadeEEstado(cidade, estado);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Atualiza um endereço
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço", description = "Atualiza os dados de um endereço existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EnderecoModel> atualizarEndereco(
            @Parameter(description = "ID do endereço")
            @PathVariable Long id,
            @Valid @RequestBody EnderecoDto enderecoDto) {
        try {
            EnderecoModel endereco = enderecoService.atualizarEndereco(id, enderecoDto);
            return ResponseEntity.ok(endereco);
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deleta um endereço
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço", description = "Remove um endereço do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<Void> deletarEndereco(
            @Parameter(description = "ID do endereço")
            @PathVariable Long id) {
        try {
            enderecoService.deletarEndereco(id);
            return ResponseEntity.noContent().build();
        } catch (EnderecoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
