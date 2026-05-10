package bizi.com.demo.infra.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Gerenciamento e visualização de arquivos de mídia (Fotos e RGs)")
public class DocumentoController {

    @Value("${bizi.storage.raiz}")
    private String raiz;

    @GetMapping("/ver/{nomeArquivo}")
    @Operation(summary = "Visualizar arquivo", description = "Recupera o arquivo físico do armazenamento local e o retorna como um stream de imagem para o navegador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo encontrado e retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "O arquivo solicitado não existe no servidor"),
            @ApiResponse(responseCode = "400", description = "Erro na formação do caminho do arquivo")
    })
    public ResponseEntity<Resource> exibirArquivo(
            @Parameter(description = "Nome exato do arquivo (incluindo UUID e extensão)") @PathVariable String nomeArquivo) {
        try {
            Path caminho = Paths.get(raiz).resolve(nomeArquivo);
            Resource recurso = new UrlResource(caminho.toUri());

            if (recurso.exists() || recurso.isReadable()) {

                // Forçamos o tipo para imagem para o navegador renderizar em vez de baixar
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}