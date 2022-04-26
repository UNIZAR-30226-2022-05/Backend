package es.unizar.unoforall.model.partidas;

import es.unizar.unoforall.model.UsuarioVO;

public class Participante {
	private UsuarioVO usuario;
	private HaJugadoVO datosPartida;
	
	public Participante (UsuarioVO usuario, HaJugadoVO datosPartida) {
		this.usuario=usuario;
		this.datosPartida=datosPartida;
	}

	public UsuarioVO getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioVO usuario) {
		this.usuario = usuario;
	}

	public HaJugadoVO getDatosPartida() {
		return datosPartida;
	}

	public void setDatosPartida(HaJugadoVO datosPartida) {
		this.datosPartida = datosPartida;
	}
	
	
}
