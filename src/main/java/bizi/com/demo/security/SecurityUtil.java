package bizi.com.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.Role;

@Component
public class SecurityUtil {

    /**
     * Recupera o objeto do usuário completo que está autenticado na requisição atual.
     */
    public UsuarioModel getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Não há nenhum usuário autenticado no sistema.");
        }

        // O principal aqui é o seu UsuarioModel (pois você o injetou no JwtFilter)
        Object principal = authentication.getPrincipal();

        if (principal instanceof UsuarioModel) {
            return (UsuarioModel) principal;
        }

        throw new RuntimeException("Erro ao recuperar os dados do usuário logado.");
    }

    /**
     * Recupera apenas a Role do usuário logado.
     */
    public Role getRoleUsuarioLogado() {
        return getUsuarioLogado().getRole();
    }

    /**
     * Verifica se o usuário logado é um Administrador.
     */
    public boolean isAdmin() {
        return getRoleUsuarioLogado() == Role.ROLE_ADMIN;
    }
}