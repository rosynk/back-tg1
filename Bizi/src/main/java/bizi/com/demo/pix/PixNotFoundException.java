package bizi.com.demo.pix;

public class PixNotFoundException extends RuntimeException {
    public PixNotFoundException(String message) {
        super(message);
    }
}