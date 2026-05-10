package bizi.com.demo.validacoes.telefone;

import org.springframework.stereotype.Service;

import bizi.com.demo.validacoes.external.TelefoneApiClient;




@Service
//@Primary (ativar no futuro)
public class TelefoneValidatorApi implements TelefoneValidator {

 private final TelefoneApiClient client;

 public TelefoneValidatorApi(TelefoneApiClient client) {
     this.client = client;
 }

 @Override
 public boolean isValid(String telefone) {
     return client.validarTelefone(telefone);
 }
}