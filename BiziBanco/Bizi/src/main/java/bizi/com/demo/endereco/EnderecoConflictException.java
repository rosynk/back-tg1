package bizi.com.demo.endereco;

public class EnderecoConflictException extends RuntimeException {
    
    public EnderecoConflictException(String message) {
        super(message);
    }
    
    public EnderecoConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
