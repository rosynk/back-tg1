package bizi.com.demo.endereco;

public class EnderecoNotFoundException extends RuntimeException {
    
    public EnderecoNotFoundException(String message) {
        super(message);
    }
    
    public EnderecoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
