package bizi.com.demo.validacoes.external;


public class EmailResponse {
    private boolean valid;
    private boolean deliverable;

    public boolean isValid() {
        return valid;
    }

    public boolean isDeliverable() {
        return deliverable;
    }
}