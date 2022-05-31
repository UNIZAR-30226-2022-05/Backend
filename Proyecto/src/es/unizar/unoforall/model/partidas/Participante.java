package es.unizar.unoforall.model.partidas;

import es.unizar.unoforall.model.UsuarioVO;

public class Participante {
	private UsuarioVO usuario;
	private HaJugadoVO datosPartida;
	private int puesto;
	private int puntos;
	
	
	public Participante (UsuarioVO usuario, HaJugadoVO datosPartida) {
		this.usuario=usuario;
		this.datosPartida=datosPartida;
		puesto = 0;
		switch(datosPartida.getUsrsDebajo()) {
		case 3:
			puntos = 20;
			break;
		case 2:
			puntos = 10;
			break;
		case 1: 
			puntos = 5;
			break;
		case 0:
			puntos = 0;
			break;
		}
	}

	
	public Participante (UsuarioVO usuario, HaJugadoVO datosPartida, int numParticipantes, int modoJuego) {
		this.usuario=usuario;
		this.datosPartida=datosPartida;
		if(modoJuego!=2) {
			puesto = numParticipantes-datosPartida.getUsrsDebajo();
		} else {
			switch(datosPartida.getUsrsDebajo()) {
			case 2:
				puesto = 1;
				break;
			case 0:
				puesto = 2;
				break;
			}
		}
		
		switch(datosPartida.getUsrsDebajo()) {
		case 3:
			puntos = 20;
			break;
		case 2:
			puntos = 10;
			break;
		case 1: 
			puntos = 5;
			break;
		case 0:
			puntos = 0;
			break;
		}
	}
	
	// Para añadir IAs solo en la finalización de partidas (no en la BD)
	public Participante (int puesto) {
		this.usuario=null;
		this.datosPartida=null;
		this.puesto = puesto;
		this.puntos = 0;
	}
	
	public int getPuesto() {
		return puesto;
	}

	public void setPuesto(int puesto) {
		this.puesto=puesto;
	}
	
	public void setPuesto(int numParticipantes, int modoJuego) {
		puesto = numParticipantes-datosPartida.getUsrsDebajo();
		if(modoJuego!=2) {
			puesto = numParticipantes-datosPartida.getUsrsDebajo();
		} else {
			switch(datosPartida.getUsrsDebajo()) {
			case 2:
				puesto = 1;
				break;
			case 0:
				puesto = 2;
				break;
			}
		}
	}

	public int getPuntos() {
		return puntos;
	}

	public void setPuntos(int puntos) {
		this.puntos = puntos;
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
