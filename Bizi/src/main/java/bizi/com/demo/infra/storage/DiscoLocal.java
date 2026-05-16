package bizi.com.demo.infra.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

@Service
@Primary
public class DiscoLocal implements Disco {

    private String raiz;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Se a raiz estiver vazia ou nula, entramos no PLANO B (Desktop)
        if (this.raiz == null || this.raiz.trim().isEmpty()) {
            String home = System.getProperty("user.home");
            this.raiz = home + File.separator + "Desktop" + File.separator + "UpdateTg2Bizi" + File.separator
                    + "DocumentosOnboarding";
            System.out.println("⚠️ PLANO B: Usando caminho padrão no Desktop: " + this.raiz);
        } else {
            System.out.println("✅ PLANO A: Usando caminho do application.properties: " + this.raiz);
        }

        // Garante a criação da pasta independente de qual plano foi escolhido
        try {
            Files.createDirectories(Paths.get(this.raiz));
        } catch (IOException e) {
            // throw new RuntimeException("Não foi possível inicializar o diretório de
            // arquivos.", e);
            System.err.println("❌ Erro crítico de permissão: " + e.getMessage());
        }
    }

    @Override
    public String salvar(MultipartFile arquivo) {
        try {
            Path diretorioPath = Paths.get(this.raiz);
            Files.createDirectories(diretorioPath);

            // LINHA DE DEBUG: Ela vai imprimir o caminho completo no console do Spring
            System.out.println(">>> ARQUIVO SENDO SALVO EM: " + diretorioPath.toAbsolutePath());

            String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
            Path arquivoPath = diretorioPath.resolve(nomeArquivo);

            arquivo.transferTo(arquivoPath.toFile());
            return nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo localmente.", e);
        }
    }

    @Override
    public void excluir(String nomeArquivo) {
        try {
            Path arquivoPath = Paths.get(this.raiz).resolve(nomeArquivo);
            Files.deleteIfExists(arquivoPath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo.");
        }
    }
    
    public String getRaiz() {
        return this.raiz;
    }
}