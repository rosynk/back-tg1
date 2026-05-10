package bizi.com.demo.validacoes.telefone;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Primary
@Service
public class TelefoneValidatorLocal implements TelefoneValidator {

	 @Override
	    public boolean isValid(String telefone) {
	        return telefone != null && telefone.matches("\\d{11}");
	    }
	}

