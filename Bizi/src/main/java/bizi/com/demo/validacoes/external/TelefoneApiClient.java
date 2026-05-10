package bizi.com.demo.validacoes.external;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TelefoneApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarTelefone(String telefone) {

        String url = "https://api.exemplo.com/telefone?numero=" + telefone;

        TelefoneResponse response =
                restTemplate.getForObject(url, TelefoneResponse.class);

        return response != null && response.isValid();
    }
}