package bizi.com.demo.usuario;

public class UsuarioConflictException extends RuntimeException {
    
    public UsuarioConflictException(String message) {
        super(message);
    }
    
    public UsuarioConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
