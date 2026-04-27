package bizi.com.demo.validacoes.email;

import org.springframework.stereotype.Service;

import bizi.com.demo.validacoes.external.EmailApiClient;



@Service
//@Primary (ativa quando quiser usar API)
public class EmailValidatorApi implements EmailValidator {

 private final EmailApiClient client;

 public EmailValidatorApi(EmailApiClient client) {
     this.client = client;
 }

 @Override
 public boolean isValid(String email) {
     return client.validarEmail(email);
 }
}
