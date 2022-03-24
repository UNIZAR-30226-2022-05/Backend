package es.unizar.unoforall.apirest;

import javax.swing.Timer;


public class SolicitudCambioContrasenya {
	private Timer timer;
	private int codigo;
	
	public SolicitudCambioContrasenya( Timer timer, Integer codigo) {
		this.timer=timer;
		this.codigo=codigo;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

}
