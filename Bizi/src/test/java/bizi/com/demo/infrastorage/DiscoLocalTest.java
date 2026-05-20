package bizi.com.demo.infrastorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import bizi.com.demo.infra.storage.DiscoLocal;

class DiscoLocalTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("init: deve criar diretório quando raiz foi informada")
    void init_deveCriarDiretorioQuandoRaizInformada() throws Exception {
        DiscoLocal disco = new DiscoLocal();
        Path raiz = tempDir.resolve("uploads");

        setRaiz(disco, raiz.toString());

        disco.init();

        assertThat(Files.exists(raiz)).isTrue();
        assertThat(disco.getRaiz()).isEqualTo(raiz.toString());
    }

    @Test
    @DisplayName("salvar: deve salvar arquivo localmente e retornar nome gerado")
    void salvar_deveSalvarArquivoLocalmenteERetornarNomeGerado() throws Exception {
        DiscoLocal disco = new DiscoLocal();
        setRaiz(disco, tempDir.toString());

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "documento.jpg",
                "image/jpeg",
                "conteudo".getBytes());

        String nomeArquivo = disco.salvar(arquivo);

        assertThat(nomeArquivo).contains("documento.jpg");
        assertThat(Files.exists(tempDir.resolve(nomeArquivo))).isTrue();
    }

    @Test
    @DisplayName("excluir: deve remover arquivo existente")
    void excluir_deveRemoverArquivoExistente() throws Exception {
        DiscoLocal disco = new DiscoLocal();
        setRaiz(disco, tempDir.toString());

        Path arquivo = tempDir.resolve("teste.txt");
        Files.writeString(arquivo, "conteudo");

        disco.excluir("teste.txt");

        assertThat(Files.exists(arquivo)).isFalse();
    }

    @Test
    @DisplayName("excluir: não deve lançar exceção quando arquivo não existe")
    void excluir_naoDeveLancarQuandoArquivoNaoExiste() throws Exception {
        DiscoLocal disco = new DiscoLocal();
        setRaiz(disco, tempDir.toString());

        assertThatCode(() -> disco.excluir("inexistente.txt"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("salvar: deve lançar RuntimeException quando transferTo falha")
    void salvar_deveLancarQuandoTransferToFalha() throws Exception {
        DiscoLocal disco = new DiscoLocal();
        setRaiz(disco, tempDir.toString());

        MultipartFile arquivo = org.mockito.Mockito.mock(MultipartFile.class);

        when(arquivo.getOriginalFilename()).thenReturn("documento.txt");
        doThrow(new IOException("falha ao transferir"))
                .when(arquivo)
                .transferTo(org.mockito.ArgumentMatchers.any(File.class));

        assertThatThrownBy(() -> disco.salvar(arquivo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao salvar arquivo localmente");
    }

    private void setRaiz(DiscoLocal disco, String valor) throws Exception {
        Field field = DiscoLocal.class.getDeclaredField("raiz");
        field.setAccessible(true);
        field.set(disco, valor);
    }
}