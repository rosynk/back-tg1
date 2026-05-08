package bizi.com.demo.usuario;

import bizi.com.demo.proposta.PropostaModel;
import bizi.com.demo.proposta.PropostaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Reforço de segurança no nível da classe
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PropostaRepository propostaRepository;

    @GetMapping("/dashboard/usuarios")
    public ResponseEntity<List<UsuarioModel>> listarTodosUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/dashboard/propostas")
    public ResponseEntity<List<PropostaModel>> listarTodasPropostas() {
        // Aqui o Admin vê quem foi reprovado por IDADE ou SCORE
        return ResponseEntity.ok(propostaRepository.findAll());
    }

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> banirUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}