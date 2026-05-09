package bizi.com.demo.infra.storage;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

//@Service
public class DiscoGoogleDrive implements Disco {

    @Autowired
    private Drive googleDriveService; // Você precisará de uma classe de Config para injetar isso

    private final String PASTA_BIZI_BANCO = "ID_DA_SUA_PASTA_NO_DRIVE";

    @Override
    public String salvar(MultipartFile arquivo) {
        try {
            File metadata = new File();
            metadata.setName(System.currentTimeMillis() + "_" + arquivo.getOriginalFilename());
            metadata.setParents(Collections.singletonList(PASTA_BIZI_BANCO));

            InputStreamContent content = new InputStreamContent(
                    arquivo.getContentType(),
                    arquivo.getInputStream());

            // Faz o upload para o Google Drive
            File arquivoGoogle = googleDriveService.files().create(metadata, content)
                    .setFields("id, webViewLink")
                    .execute();

            // Retornamos o ID do arquivo ou o Link para salvar no MySQL
            return arquivoGoogle.getWebViewLink();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao subir arquivo para o Google Drive", e);
        }
    }

    @Override
    public void excluir(String urlOuId) {
        try {
            // Extrai o ID da URL ou usa o ID direto para deletar
            String fileId = extrairIdDaUrl(urlOuId);
            googleDriveService.files().delete(fileId).execute();
            System.out.println("Arquivo deletado do Drive: " + fileId);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir arquivo do Google Drive", e);
        }
    }

    private String extrairIdDaUrl(String url) {
        // Lógica simples para pegar o ID de uma URL do Google Drive
        // Exemplo: https://drive.google.com/file/d/ID_AQUI/view
        return url.split("/d/")[1].split("/")[0];
    }
}