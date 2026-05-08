package bizi.com.demo.saque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bizi.com.demo.transacao.TransacaoModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/saque")
@Tag(name = "Saque", description = "Operações de retirada de dinheiro")
public class SaqueController {

    @Autowired
    private SaqueService saqueService;

    // ENDPOINT PARA O CLIENTE (LOGADO)
    @PostMapping
    @Operation(summary = "Realizar saque (Cliente)", description = "Debita valor da conta do usuário logado pelo Token.")
    public ResponseEntity<?> efetuarSaque(@RequestBody Map<String, BigDecimal> payload) {
        try {
            BigDecimal valor = payload.get("valor");
            TransacaoModel resultado = saqueService.realizarSaque(valor);
            return ResponseEntity.ok("Saque realizado! Saldo atual: R$ " + resultado.getContaBancaria().getSaldo());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- NOVO ENDPOINT: O "SUPERPODER" DO ADM ---
    @PostMapping("/admin/{idConta}")
    @Operation(summary = "[ADM] Saque Administrativo", description = "Permite ao ADM sacar de qualquer conta pelo ID.")
    public ResponseEntity<?> saqueAdministrativo(@PathVariable Long idConta, @RequestBody Map<String, BigDecimal> payload) {
        try {
            BigDecimal valor = payload.get("valor");
            TransacaoModel resultado = saqueService.realizarSaqueAdministrativo(idConta, valor);
            return ResponseEntity.ok("Saque administrativo na conta " + idConta + " realizado! Saldo: R$ " + resultado.getContaBancaria().getSaldo());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}