package bizi.com.demo.validacoes.external;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CpfApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarCpf(String cpf) {
        String url = "https://api.exemplo.com/cpf/" + cpf;
        Boolean response = restTemplate.getForObject(url, Boolean.class);
        return response != null && response;
    }
}