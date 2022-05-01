package es.unizar.unoforall.gestores.apirest;

import javax.swing.Timer;

import es.unizar.unoforall.model.UsuarioVO;

public class RegistroTemporal {

	private UsuarioVO usuario;
	private Timer timer;
	private int codigo;
	
	public RegistroTemporal(UsuarioVO usuario, Timer timer, Integer codigo) {
		this.usuario=usuario;
		this.timer=timer;
		this.codigo=codigo;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public UsuarioVO getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioVO miUsuario) {
		this.usuario = miUsuario;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer miTimer) {
		this.timer = miTimer;
	}
}
