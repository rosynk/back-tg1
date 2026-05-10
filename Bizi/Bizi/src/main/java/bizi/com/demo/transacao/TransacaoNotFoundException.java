package bizi.com.demo.transacao;

public class TransacaoNotFoundException extends RuntimeException {
    public TransacaoNotFoundException(String message) {
        super(message);
    }
}

class TransacaoConflictException extends RuntimeException {
    public TransacaoConflictException(String message) {
        super(message);
    }
}