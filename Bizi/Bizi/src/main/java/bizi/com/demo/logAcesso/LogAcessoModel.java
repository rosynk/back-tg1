package bizi.com.demo.logAcesso;

import java.time.LocalDateTime;

import bizi.com.demo.usuario.UsuarioModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_acesso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAcessoModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_log_acesso")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_usuario", nullable = false)
	private UsuarioModel usuario;

	@Column(name = "data_hora_acesso", nullable = false)
	private LocalDateTime dataHoraAcesso;

	@Column(name = "ip_origem", nullable = false)
	private String ipOrigem;

	private String acao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UsuarioModel getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioModel usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getDataHoraAcesso() {
		return dataHoraAcesso;
	}

	public void setDataHoraAcesso(LocalDateTime dataHoraAcesso) {
		this.dataHoraAcesso = dataHoraAcesso;
	}

	public String getIpOrigem() {
		return ipOrigem;
	}

	public void setIpOrigem(String ipOrigem) {
		this.ipOrigem = ipOrigem;
	}

	public String getAcao() {
		return acao;
	}

	public void setAcao(String acao) {
		this.acao = acao;
	}
}
