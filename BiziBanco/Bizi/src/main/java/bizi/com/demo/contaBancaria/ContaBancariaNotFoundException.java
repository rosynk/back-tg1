package bizi.com.demo.contaBancaria;

/**
 * Exceção lançada quando uma conta não é encontrada
 */
public class ContaBancariaNotFoundException extends RuntimeException {
    public ContaBancariaNotFoundException(String message) {
        super(message);
    }
}

/**
 * Exceção lançada quando há conflito ao criar/atualizar conta
 */
class ContaBancariaConflictException extends RuntimeException {
    public ContaBancariaConflictException(String message) {
        super(message);
    }
}