package bizi.com.demo.infra.storage;

import org.springframework.web.multipart.MultipartFile;

public interface Disco {
    String salvar(MultipartFile arquivo);

    void excluir(String caminho);
}