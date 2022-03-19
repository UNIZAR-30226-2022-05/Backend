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

	public UsuarioVO getMiUsuario() {
		return usuario;
	}

	public void setMiUsuario(UsuarioVO miUsuario) {
		this.usuario = miUsuario;
	}

	public Timer getMiTimer() {
		return timer;
	}

	public void setMiTimer(Timer miTimer) {
		this.timer = miTimer;
	}
}
