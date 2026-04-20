package bizi.com.demo.pagamentoBoleto;

public class PagamentoBoletoNotFoundException extends RuntimeException {
    public PagamentoBoletoNotFoundException(String message) {
        super(message);
    }
}