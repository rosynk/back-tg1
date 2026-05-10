package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.security.SecurityUtil;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;
import bizi.com.demo.usuario.UsuarioRepository;

@Service
public class ContaBancariaService {

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional
    public ContaBancariaModel criarConta(ContaBancariaDto dto) {
        UsuarioModel usuario;

        if (dto.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado."));
        } else {
            usuario = securityUtil.getUsuarioLogado();
        }

        ContaBancariaModel conta = new ContaBancariaModel();
        conta.setUsuario(usuario);
        conta.setNumeroAgencia(dto.getNumeroAgencia() != null ? dto.getNumeroAgencia() : "0001");
        conta.setNumeroConta(gerarNumeroContaUnico());
        conta.setTipoConta(dto.getTipoConta());
        conta.setStatusConta(true);
        conta.setSaldo(BigDecimal.ZERO);
        conta.setDataCriacao(LocalDateTime.now());

        return contaBancariaRepository.save(conta);
    }

    // --- LOGÍSTICA DE DEPÓSITO ---

    @Transactional
    public void realizarAutoDepositoLogado(BigDecimal valor) {
        validarValorPositivo(valor);

        // CORREÇÃO: Pega o ID do usuário através do Token de forma isolada
        Long usuarioId = securityUtil.getUsuarioLogado().getId();

        // Busca a conta vinculada a este usuário logado
        ContaBancariaModel conta = contaBancariaRepository.findByUsuarioId(usuarioId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nenhuma conta encontrada para o seu usuário."));

        conta.setSaldo(conta.getSaldo().add(valor));
        contaBancariaRepository.save(conta);
    }

    @Transactional
    public void depositar(Long contaId, BigDecimal valor) {
        validarValorPositivo(valor);
        ContaBancariaModel conta = buscarPorId(contaId);
        conta.setSaldo(conta.getSaldo().add(valor));
        contaBancariaRepository.save(conta);
    }

    @Transactional
    public void autoDeposito(Long contaId, BigDecimal valor) {
        validarValorPositivo(valor);
        Long usuarioLogadoId = securityUtil.getUsuarioLogado().getId();
        ContaBancariaModel conta = buscarPorId(contaId);

        if (!conta.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new RuntimeException("Você não tem permissão para depositar nesta conta.");
        }

        conta.setSaldo(conta.getSaldo().add(valor));
        contaBancariaRepository.save(conta);
    }

    // --- LOGÍSTICA DE TRANSFERÊNCIA (PIX) ---

    @Transactional
    public void transferir(Long origemId, Long destinoId, BigDecimal valor) {
        validarValorPositivo(valor);
        if (origemId.equals(destinoId))
            throw new RuntimeException("Contas iguais.");

        ContaBancariaModel origem = buscarPorId(origemId);
        ContaBancariaModel destino = buscarPorId(destinoId);

        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));

        contaBancariaRepository.save(origem);
        contaBancariaRepository.save(destino);
    }

    // --- LOGÍSTICA DE TRANSFERÊNCIA (TED) ---

    @Transactional
    public void transferirViaTed(Long origemId, Long destinoId, BigDecimal valor) {
        LocalDateTime agora = LocalDateTime.now();
        DayOfWeek diaDaSemana = agora.getDayOfWeek();
        int hora = agora.getHour();

        if (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY) {
            throw new RuntimeException("TED indisponível aos finais de semana. Utilize o Pix!");
        }

        if (hora < 9 || hora >= 17) {
            throw new RuntimeException("Horário de TED encerrado (09h às 17h). Utilize o Pix!");
        }

        this.transferir(origemId, destinoId, valor);
    }

    // --- MÉTODOS DE BUSCA E MANUTENÇÃO ---

    @Transactional(readOnly = true)
    public List<ContaBancariaModel> listarTodas() {
        return contaBancariaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ContaBancariaModel buscarPorId(Long id) {
        return contaBancariaRepository.findById(id)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<ContaBancariaModel> buscarMinhasContas() {
        // CORREÇÃO: Garante que busca apenas as contas do ID que está no TOKEN
        return contaBancariaRepository.findByUsuarioId(securityUtil.getUsuarioLogado().getId());
    }

    @Transactional
    public void deletarConta(Long id) {
        if (!contaBancariaRepository.existsById(id))
            throw new ContaBancariaNotFoundException("Inexistente.");
        contaBancariaRepository.deleteById(id);
    }

    // --- AUXILIARES ---

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor deve ser maior que zero.");
        }
    }

    private String gerarNumeroContaUnico() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
    
    @Transactional(readOnly = true)
    public ContaBancariaModel buscarPorUsuario(Long usuarioId) {
        return contaBancariaRepository.findByUsuarioId(usuarioId)
                .stream()
                .findFirst()
                .orElse(null); 
    }

    // Método para salvar alterações em uma conta existente
    @Transactional
    public void atualizarConta(ContaBancariaModel conta) {
        contaBancariaRepository.save(conta);
    }
}