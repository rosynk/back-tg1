package bizi.com.demo.proposta;

public class PropostaResponseDto {
    private Long usuarioId;
    private Long contaId;
    private String status;
    private String mensagem;

    // ✅ Novos campos para o cliente confirmar o que foi enviado
    private String urlSelfieConfirmada;

    public PropostaResponseDto() {
    }

    public PropostaResponseDto(Long usuarioId, Long contaId, String status, String mensagem,
            String urlSelfieConfirmada) {
        this.usuarioId = usuarioId;
        this.contaId = contaId;
        this.status = status;
        this.mensagem = mensagem;
        this.urlSelfieConfirmada = urlSelfieConfirmada;
    }

    // Getters e Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getContaId() {
        return contaId;
    }

    public void setContaId(Long contaId) {
        this.contaId = contaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getUrlSelfieConfirmada() {
        return urlSelfieConfirmada;
    }

    public void setUrlSelfieConfirmada(String urlSelfieConfirmada) {
        this.urlSelfieConfirmada = urlSelfieConfirmada;
    }
}