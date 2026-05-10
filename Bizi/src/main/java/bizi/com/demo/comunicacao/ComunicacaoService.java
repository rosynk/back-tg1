package bizi.com.demo.comunicacao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ComunicacaoService {

    @Autowired(required = false)
    private JavaMailSender mailSender; // O 'required=false' evita erro se você comentar as propriedades

    public void enviarEmailRecuperacao(String email, String codigo) {
        boolean enviadoComSucesso = false;

        // Tenta enviar pelo Gmail Real
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("seu-email@gmail.com"); 
                message.setTo(email);
                message.setSubject("Bizi - Código de Recuperação");
                message.setText("Seu código de segurança Bizi é: " + codigo);
                
                mailSender.send(message);
                enviadoComSucesso = true;
                System.out.println("✅ [GMAIL] E-mail real enviado para: " + email);
            }
        } catch (Exception e) {
            System.err.println("❌ [GMAIL] Falha no envio real: " + e.getMessage());
            System.out.println("⚠️ Ativando BACKUP para console...");
        }

        // Se o envio real falhou OU se você não configurou o e-mail, ele faz o MOCK
        if (!enviadoComSucesso) {
            exibirNoConsole(email, codigo);
        }
    }

    private void exibirNoConsole(String email, String codigo) {
        System.out.println("\n***************************************************");
        System.out.println("* [MODO DESENVOLVEDOR - BACKUP CONSOLE]           *");
        System.out.println("* PARA: " + email);
        System.out.println("* CÓDIGO GERADO: " + codigo);
        System.out.println("* (Use este código no Swagger para redefinir)     *");
        System.out.println("***************************************************\n");
    }
}