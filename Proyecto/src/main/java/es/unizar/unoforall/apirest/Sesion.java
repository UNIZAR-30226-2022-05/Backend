package es.unizar.unoforall.apirest;

import javax.swing.Timer;

import es.unizar.unoforall.model.UsuarioVO;

public class Sesion {
	
	private UsuarioVO usuario;
	private Timer timer;
	
	public Sesion(UsuarioVO usuario, Timer timer) {
		this.usuario=usuario;
		this.timer=timer;
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
