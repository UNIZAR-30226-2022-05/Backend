package es.unizar.unoforall.apirest;

import javax.swing.Timer;

import es.unizar.unoforall.model.UsuarioVO;

public class Sesion {
	
	private UsuarioVO miUsuario;
	private Timer miTimer;
	
	public Sesion(UsuarioVO usuario, Timer timer) {
		miUsuario=usuario;
		miTimer=timer;
	}

	public UsuarioVO getMiUsuario() {
		return miUsuario;
	}

	public void setMiUsuario(UsuarioVO miUsuario) {
		this.miUsuario = miUsuario;
	}

	public Timer getMiTimer() {
		return miTimer;
	}

	public void setMiTimer(Timer miTimer) {
		this.miTimer = miTimer;
	}
}
