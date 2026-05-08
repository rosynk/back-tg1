package bizi.com.demo.contaBancaria;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContaInexistenteException extends RuntimeException {
    public ContaInexistenteException(String mensagem) {
        super(mensagem);
    }
}