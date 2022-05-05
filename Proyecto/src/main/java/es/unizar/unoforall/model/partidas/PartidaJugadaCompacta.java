package es.unizar.unoforall.model.partidas;

import java.sql.Date;
import java.util.List;

import es.unizar.unoforall.model.salas.ConfigSala;

public class PartidaJugadaCompacta{
	private static final ConfigSala.ModoJuego[] modosJuego = ConfigSala.ModoJuego.values();
	
	private Date fechaInicio;
	private Date fechaFin;
	private ConfigSala.ModoJuego modoJuego;
	private List<Participante> participantes;
	
	public PartidaJugadaCompacta(PartidaJugada partidaJugada){
		this.fechaInicio = partidaJugada.getPartida().getFechaInicioPartida();
		this.fechaFin = partidaJugada.getPartida().getFechaFinPartida();
		this.modoJuego = modosJuego[partidaJugada.getPartida().getModoJuego()];
		this.participantes = partidaJugada.getParticipantes();
	}

	public Date getFechaInicio(){
		return fechaInicio;
	}

	public Date getFechaFin(){
		return fechaFin;
	}

	public ConfigSala.ModoJuego getModoJuego(){
		return modoJuego;
	}

	public List<Participante> getParticipantes(){
		return participantes;
	}
}
