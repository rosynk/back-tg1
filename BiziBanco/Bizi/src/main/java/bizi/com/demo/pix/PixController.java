package bizi.com.demo.pix;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pix")
@Tag(name = "Pix", description = "Operações de envio e consulta de Pix")
public class PixController {

    @Autowired
    private PixService pixService;

    @PostMapping("/enviar")
    @Operation(summary = "Realizar envio de Pix", 
               description = "Debita da conta do usuário logado e registra o envio para a chave destino (CPF ou Email).")
    public ResponseEntity<?> enviarPix(@Valid @RequestBody PixDto dto) {
        try {
            // Agora o Controller passa o DTO completo para o Service
            PixModel pixRealizado = pixService.realizarPix(dto);
            return ResponseEntity.ok(pixRealizado);
        } catch (RuntimeException e) {
            // Captura erros de saldo insuficiente ou chave não encontrada
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/meus-envios")
    @Operation(summary = "Listar meus Pix enviados", 
               description = "Retorna o histórico de Pix realizados pelo usuário logado.")
    public ResponseEntity<List<PixModel>> listarMeusPix() {
        return ResponseEntity.ok(pixService.listarPixDoUsuarioLogado());
    }
}