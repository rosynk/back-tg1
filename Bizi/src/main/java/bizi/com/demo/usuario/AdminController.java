package bizi.com.demo.usuario;

import bizi.com.demo.proposta.PropostaModel;
import bizi.com.demo.proposta.PropostaRepository;
import bizi.com.demo.proposta.PropostaService;
import bizi.com.demo.proposta.StatusProposta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Controller", description = "Endpoints de gestão e auditoria do Bizi Banco (Back-office)")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private PropostaService propostaService;

    @Operation(summary = "Lista todos os usuários", description = "Retorna a base completa de usuários cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioModel.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ROLE_ADMIN")
    })
    @GetMapping("/dashboard/usuarios")
    public ResponseEntity<List<UsuarioModel>> listarTodosUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @Operation(summary = "Lista todas as propostas", description = "O ADM utiliza este endpoint para visualizar o histórico de propostas, documentos e selfies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propostas recuperadas com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PropostaModel.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/dashboard/propostas")
    public ResponseEntity<List<PropostaModel>> listarTodasPropostas() {
        return ResponseEntity.ok(propostaRepository.findAll());
    }

    @Operation(summary = "Decisão Administrativa", description = "Aprova ou nega uma proposta. Se aprovada, ativa o usuário. Se negada, exclui arquivos (LGPD).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status da proposta atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropostaModel.class))),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de status inválidos")
    })
    @PatchMapping("/propostas/{id}/decisao")
    public ResponseEntity<PropostaModel> decidirProposta(
            @Parameter(description = "ID da proposta a ser analisada") @PathVariable Long id,
            @Parameter(description = "Novo status: APROVADA ou NEGADA") @RequestParam StatusProposta status,
            @Parameter(description = "Motivo da decisão (Obrigatório para transparência)") @RequestParam String observacao) {

        PropostaModel propostaAtualizada = propostaService.avaliarProposta(id, status, observacao);
        return ResponseEntity.ok(propostaAtualizada);
    }

    @Operation(summary = "Banir Usuário", description = "Remove permanentemente um usuário do banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "ID de usuário não localizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> banirUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}