package bizi.com.demo.validacoes.CPF;

import org.springframework.stereotype.Service;
import bizi.com.demo.validacoes.external.CpfApiClient;

@Service
public class CpfValidatorApi implements CPFValidador {

    private final CpfApiClient client;

    public CpfValidatorApi(CpfApiClient client) {
        this.client = client;
    }

    @Override
    public boolean isValid(String cpf) {
        try {
            // Aqui ele tentará chamar sua classe CpfApiClient
            return client.validarCpf(cpf);
        } catch (Exception e) {
            // Se a API estiver fora do ar ou não configurada, 
            // ele pode retornar false ou lançar um erro personalizado
            return false; 
        }
    }
}