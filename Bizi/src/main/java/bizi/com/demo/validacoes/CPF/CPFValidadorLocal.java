package bizi.com.demo.validacoes.CPF;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class CPFValidadorLocal implements CPFValidador {

	@Override
	public boolean isValid(String cpf) {
		

        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        // evita CPF com números iguais (11111111111, etc)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // cálculo dos dígitos verificadores
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }

        int digito1 = 11 - (soma % 11);
        if (digito1 >= 10) digito1 = 0;

        if (digito1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }

        int digito2 = 11 - (soma % 11);
        if (digito2 >= 10) digito2 = 0;

        return digito2 == (cpf.charAt(10) - '0');
   
	}

	
	
}
