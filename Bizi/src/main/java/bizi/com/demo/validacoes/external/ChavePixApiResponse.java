package bizi.com.demo.validacoes.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChavePixApiResponse {

    private boolean sucesso;
    private String mensagem;

    @JsonProperty("data")
    private Object dados;

    // 1. Construtor Padrão (Sem argumentos) - Importante para o Jackson/Spring
    public ChavePixApiResponse() {
    }

    // 2. Construtor Completo (O que o seu Controller está pedindo)
    public ChavePixApiResponse(boolean sucesso, String mensagem, Object dados) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dados = dados;
    }

    // --- Seus Getters e Setters Manuais (Mantenha-os abaixo) ---

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Object getDados() {
        return dados;
    }

    public void setDados(Object dados) {
        this.dados = dados;
    }
}