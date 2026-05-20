package bizi.com.demo.infrastorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import bizi.com.demo.infra.storage.DiscoGoogleDrive;

class DiscoGoogleDriveTest {

    @Test
    @DisplayName("salvar: deve subir arquivo no Google Drive e retornar webViewLink")
    void salvar_deveSubirArquivoERetornarLink() throws Exception {
        Drive drive = Mockito.mock(Drive.class);
        Drive.Files files = Mockito.mock(Drive.Files.class);
        Drive.Files.Create create = Mockito.mock(Drive.Files.Create.class);

        DiscoGoogleDrive disco = new DiscoGoogleDrive();
        setGoogleDriveService(disco, drive);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "documento.pdf",
                "application/pdf",
                "conteudo".getBytes());

        File arquivoGoogle = new File();
        arquivoGoogle.setWebViewLink("https://drive.google.com/file/d/abc/view");

        when(drive.files()).thenReturn(files);
        when(files.create(any(File.class), any(InputStreamContent.class))).thenReturn(create);
        when(create.setFields("id, webViewLink")).thenReturn(create);
        when(create.execute()).thenReturn(arquivoGoogle);

        String resultado = disco.salvar(arquivo);

        assertThat(resultado).isEqualTo("https://drive.google.com/file/d/abc/view");
        verify(create).execute();
    }

    @Test
    @DisplayName("salvar: deve lançar RuntimeException quando upload falha")
    void salvar_deveLancarQuandoUploadFalha() throws Exception {
        Drive drive = Mockito.mock(Drive.class);
        Drive.Files files = Mockito.mock(Drive.Files.class);
        Drive.Files.Create create = Mockito.mock(Drive.Files.Create.class);

        DiscoGoogleDrive disco = new DiscoGoogleDrive();
        setGoogleDriveService(disco, drive);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "documento.pdf",
                "application/pdf",
                "conteudo".getBytes());

        when(drive.files()).thenReturn(files);
        when(files.create(any(File.class), any(InputStreamContent.class))).thenReturn(create);
        when(create.setFields("id, webViewLink")).thenReturn(create);
        when(create.execute()).thenThrow(new IOException("falha drive"));

        assertThatThrownBy(() -> disco.salvar(arquivo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao subir arquivo para o Google Drive");
    }

    @Test
    @DisplayName("excluir: deve extrair ID da URL e deletar arquivo")
    void excluir_deveExtrairIdDaUrlEDeletar() throws Exception {
        Drive drive = Mockito.mock(Drive.class);
        Drive.Files files = Mockito.mock(Drive.Files.class);
        Drive.Files.Delete delete = Mockito.mock(Drive.Files.Delete.class);

        DiscoGoogleDrive disco = new DiscoGoogleDrive();
        setGoogleDriveService(disco, drive);

        when(drive.files()).thenReturn(files);
        when(files.delete("ID_AQUI")).thenReturn(delete);

        disco.excluir("https://drive.google.com/file/d/ID_AQUI/view");

        verify(files).delete("ID_AQUI");
        verify(delete).execute();
    }

    @Test
    @DisplayName("excluir: deve lançar RuntimeException quando delete falha")
    void excluir_deveLancarQuandoDeleteFalha() throws Exception {
        Drive drive = Mockito.mock(Drive.class);
        Drive.Files files = Mockito.mock(Drive.Files.class);
        Drive.Files.Delete delete = Mockito.mock(Drive.Files.Delete.class);

        DiscoGoogleDrive disco = new DiscoGoogleDrive();
        setGoogleDriveService(disco, drive);

        when(drive.files()).thenReturn(files);
        when(files.delete("ID_AQUI")).thenReturn(delete);
        when(delete.execute()).thenThrow(new IOException("falha delete"));

        assertThatThrownBy(() -> disco.excluir("https://drive.google.com/file/d/ID_AQUI/view"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao excluir arquivo do Google Drive");
    }

    private void setGoogleDriveService(DiscoGoogleDrive disco, Drive drive) throws Exception {
        Field field = DiscoGoogleDrive.class.getDeclaredField("googleDriveService");
        field.setAccessible(true);
        field.set(disco, drive);
    }
}