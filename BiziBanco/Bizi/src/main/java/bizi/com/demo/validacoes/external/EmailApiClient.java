package bizi.com.demo.validacoes.external;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarEmail(String email) {

        String url = "https://api.exemplo.com/email?value=" + email;

        EmailResponse response =
                restTemplate.getForObject(url, EmailResponse.class);

        return response != null && response.isValid();
    }
}