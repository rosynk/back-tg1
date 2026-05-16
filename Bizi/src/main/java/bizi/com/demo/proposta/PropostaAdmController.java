package bizi.com.demo.proposta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/propostas")
public class PropostaAdmController {

    @Autowired
    private PropostaService propostaService;

    @Autowired
    private PropostaRepository propostaRepository;

    @GetMapping
    public ResponseEntity<List<PropostaModel>> listarTodas() {
        return ResponseEntity.ok(propostaRepository.findAll());
    }

    @PutMapping("/{id}/avaliar")
    public ResponseEntity<PropostaModel> avaliar(
            @PathVariable Long id,
            @RequestBody AvaliacaoRequest req) {
        return ResponseEntity.ok(
            propostaService.avaliarProposta(id, req.novoStatus(), req.observacao())
        );
    }
}