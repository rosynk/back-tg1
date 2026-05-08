package bizi.com.demo.validacoes.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor

public class ChavePixApiResponse {
    private boolean sucesso;
    private String mensagem;

    @JsonProperty("data") // Isso faz o JSON sair como "data" mesmo o atributo sendo "dados"
    private Object dados;
}