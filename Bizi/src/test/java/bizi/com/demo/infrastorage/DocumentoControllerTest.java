package bizi.com.demo.infrastorage;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.infra.storage.DiscoLocal;
import bizi.com.demo.infra.storage.DocumentoController;

class DocumentoControllerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("exibirArquivo: deve retornar 200 quando arquivo existe e é legível")
    void exibirArquivo_deveRetornar200QuandoArquivoExiste() throws Exception {
        Path arquivo = tempDir.resolve("documento.jpg");
        Files.writeString(arquivo, "conteudo");

        DocumentoController controller = new DocumentoController();
        DiscoLocal discoLocal = new DiscoLocal();
        setRaiz(discoLocal, tempDir.toString());
        setDiscoLocal(controller, discoLocal);

        ResponseEntity<Resource> response = controller.exibirArquivo("documento.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"documento.jpg\"");
    }

    @Test
    @DisplayName("exibirArquivo: deve retornar 404 quando arquivo não existe")
    void exibirArquivo_deveRetornar404QuandoArquivoNaoExiste() throws Exception {
        DocumentoController controller = new DocumentoController();
        DiscoLocal discoLocal = new DiscoLocal();
        setRaiz(discoLocal, tempDir.toString());
        setDiscoLocal(controller, discoLocal);

        ResponseEntity<Resource> response = controller.exibirArquivo("inexistente.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void setDiscoLocal(DocumentoController controller, DiscoLocal discoLocal) throws Exception {
        Field field = DocumentoController.class.getDeclaredField("discoLocal");
        field.setAccessible(true);
        field.set(controller, discoLocal);
    }

    private void setRaiz(DiscoLocal discoLocal, String raiz) throws Exception {
        Field field = DiscoLocal.class.getDeclaredField("raiz");
        field.setAccessible(true);
        field.set(discoLocal, raiz);
    }
}