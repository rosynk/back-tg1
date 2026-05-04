package bizi.com.demo.pix;

import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.validacoes.external.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pix")
@Tag(name = "Operações Pix", description = "Serviços de transferência instantânea do Bizi Bank")
public class PixController {

    @Autowired
    private PixService pixService;

    @GetMapping("/conta") // ✅ O caminho final fica /api/pix/conta
    public ResponseEntity<?> getDadosConta() {
        // Sua lógica para buscar os dados que o Angular espera
        // Certifique-se de que o objeto retornado tenha a estrutura:
        // { saldo: 0, extrato: { transacoes: [] } }
        return ResponseEntity.ok(pixService.buscarInformacoesResumo());
    }

    @Operation(summary = "Executar transferência Pix", description = "Realiza o débito na conta logada e crédito na conta vinculada à chave informada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente, chave não encontrada ou dados inválidos", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no processamento da transação", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class)))
    })
    @PostMapping("/transferir")
    public ResponseEntity<ChavePixApiResponse> enviar(@Valid @RequestBody PixDto dto) {
        try {
            // Chamada ao Service utilizando o nome correto do método
            PixModel resultado = pixService.realizarPix(dto);
            return ResponseEntity.ok(new ChavePixApiResponse(true, "Pix enviado com sucesso!", resultado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChavePixApiResponse(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "Histórico de Pix", description = "Retorna a lista de todas as operações Pix enviadas pelo usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transações recuperada com sucesso", content = @Content(schema = @Schema(implementation = ChavePixApiResponse.class)))
    })
    @GetMapping("/historico")
    public ResponseEntity<ChavePixApiResponse> listarHistorico() {
        try {
            List<PixModel> historico = pixService.listarPixDoUsuarioLogado();
            return ResponseEntity.ok(new ChavePixApiResponse(true, "Histórico recuperado.", historico));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChavePixApiResponse(false, "Erro ao buscar histórico: " + e.getMessage(), null));
        }
    }

}