package bizi.com.demo.pix;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bizi.com.demo.contaBancaria.ContaBancariaModel;

@Repository
public interface PixRepository extends JpaRepository<PixModel, Long> {
    // O "_" indica ao Spring para entrar no objeto transacao e buscar a contaBancaria
    List<PixModel> findByTransacao_ContaBancaria(ContaBancariaModel conta);
}