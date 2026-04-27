package bizi.com.demo.validacoes.external;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ViaCepClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public ViaCepResponse buscarEnderecoPorCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        try {
            return restTemplate.getForObject(url, ViaCepResponse.class);
        } catch (Exception e) {
            return null; // No futuro, podemos tratar erros de conexão aqui
        }
    }

    /**
     * Classe DTO para mapear o JSON do ViaCEP
     */
    public static class ViaCepResponse {
        private String logradouro;
        private String bairro;
        private String localidade; // Cidade
        private String uf;         // Estado
        private String cep;
        private boolean erro;

        // --- GETTERS E SETTERS MANUAIS ---

        public String getLogradouro() {
            return logradouro;
        }

        public void setLogradouro(String logradouro) {
            this.logradouro = logradouro;
        }

        public String getBairro() {
            return bairro;
        }

        public void setBairro(String bairro) {
            this.bairro = bairro;
        }

        public String getLocalidade() {
            return localidade;
        }

        public void setLocalidade(String localidade) {
            this.localidade = localidade;
        }

        public String getUf() {
            return uf;
        }

        public void setUf(String uf) {
            this.uf = uf;
        }

        public String getCep() {
            return cep;
        }

        public void setCep(String cep) {
            this.cep = cep;
        }

        public boolean isErro() {
            return erro;
        }

        public void setErro(boolean erro) {
            this.erro = erro;
        }
    }
}