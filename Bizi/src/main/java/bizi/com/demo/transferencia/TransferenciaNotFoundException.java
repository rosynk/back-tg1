package bizi.com.demo.transferencia;

public class TransferenciaNotFoundException extends RuntimeException {
    public TransferenciaNotFoundException(String message) {
        super(message);
    }
}


class TransferenciaException extends RuntimeException {
    public TransferenciaException(String message) {
        super(message);
    }
}