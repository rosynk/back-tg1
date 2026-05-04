package bizi.com.demo.chavePix;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.validacoes.external.ChavePixApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/chaves-pix") // Rota base: /api/chaves-pix
@Tag(name = "Gerenciamento de Chaves Pix", description = "Serviços para gerenciamento do endereçamento Pix do Bizi Bank")
public class ChavePixController {

    @Autowired
    private ChavePixService service;

    @Operation(summary = "Cadastrar nova chave", description = "Registra uma nova chave vinculada à conta do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave criada com sucesso", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro na requisição ou chave duplicada", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ChavePixApiResponse> cadastrar(
            @AuthenticationPrincipal UsuarioModel usuarioLogado,
            @RequestBody ChavePixDto dto) {
        try {
            Long contaIdReal = service.buscarIdContaPorUsuario(usuarioLogado.getCpf());
            var novaChave = service.cadastrarChave(contaIdReal, dto.tipo(), dto.valor());

            return ResponseEntity.ok(new ChavePixApiResponse(true, "Chave cadastrada!", novaChave));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChavePixApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/conta") // Rota completa: /api/chaves-pix/conta
    public ResponseEntity<ChavePixApiResponse> buscarDadosConta(@AuthenticationPrincipal UsuarioModel usuarioLogado) {
        try {
            // Lógica para buscar os dados da conta usando o CPF do usuário logado
            var dadosConta = service.buscarDetalhesDaConta(usuarioLogado.getCpf());
            return ResponseEntity.ok(new ChavePixApiResponse(true, "Dados da conta recuperados", dadosConta));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ChavePixApiResponse(false, "Conta não encontrada", null));
        }
    }

    @Operation(summary = "Listar chaves", description = "Retorna todas as chaves cadastradas para o usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class)))
    })
    @GetMapping("/chaves")
    public ResponseEntity<ChavePixApiResponse> listar(@AuthenticationPrincipal UsuarioModel usuarioLogado) {
        // 1. Identificação do Usuário (Monitoramento de Segurança)
        System.out.println("🔍 [DEBUG BIZI] Iniciando listagem para CPF: "
                + (usuarioLogado != null ? usuarioLogado.getCpf() : "NULO"));

        try {
            if (usuarioLogado == null) {
                System.err.println("❌ [DEBUG BIZI] Falha: usuarioLogado é NULL. Verifique o Contexto de Segurança.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ChavePixApiResponse(false, "Usuário não autenticado.", null));
            }

            // 2. Localização da Conta (Vinculação de Entidades)
            Long contaIdReal = service.buscarIdContaPorUsuario(usuarioLogado.getCpf());
            System.out.println("🆔 [DEBUG BIZI] ID da Conta localizado: " + contaIdReal);

            if (contaIdReal == null) {
                System.out.println("⚠️ [DEBUG BIZI] Nenhuma conta encontrada para este CPF.");
                return ResponseEntity
                        .ok(new ChavePixApiResponse(true, "Usuário sem conta vinculada.", new ArrayList<>()));
            }

            // 3. Consulta ao Banco (Validação do Repository)
            List<ChavePixModel> chaves = service.listarChavesPorConta(contaIdReal);
            System.out.println("📊 [DEBUG BIZI] Quantidade de chaves retornadas do Repository: "
                    + (chaves != null ? chaves.size() : "NULL"));

            // 4. Verificação de Dados Brutos (Checklist de Conteúdo)
            if (chaves != null && !chaves.isEmpty()) {
                chaves.forEach(c -> System.out
                        .println("🔑 [DEBUG BIZI] Chave encontrada: ID=" + c.getId() + " | Valor=" + c.getValor()));
            } else {
                System.out.println(
                        "ℹ️ [DEBUG BIZI] O banco de dados retornou uma lista vazia para a conta " + contaIdReal);
            }

            // 5. Resposta Final (Checklist de Serialização)
            return ResponseEntity.ok(new ChavePixApiResponse(true, "Busca concluída.", chaves));

        } catch (Exception e) {
            System.err.println("🔥 [DEBUG BIZI] Erro Crítico no Controller: " + e.getMessage());
            e.printStackTrace(); // Log completo da stack para debug profundo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChavePixApiResponse(false, "Erro ao listar: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Excluir chave", description = "Remove permanentemente uma chave Pix pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave removida com sucesso", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ChavePixApiResponse> excluir(@PathVariable Long id) {
        try {
            service.removerChave(id);
            return ResponseEntity.ok(new ChavePixApiResponse(true, "Chave removida com sucesso!", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ChavePixApiResponse(false, "Não foi possível remover: Chave inexistente.", null));
        }
    }
}