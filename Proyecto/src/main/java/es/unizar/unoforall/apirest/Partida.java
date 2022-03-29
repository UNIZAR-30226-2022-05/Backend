package es.unizar.unoforall.apirest;

import java.util.ArrayList;

import es.unizar.unoforall.model.HaJugadoVO;
import es.unizar.unoforall.model.PartidasAcabadasVO;

public class Partida {

	private PartidasAcabadasVO partida;
	private ArrayList<HaJugadoVO> participantes = null;
	
	public Partida (PartidasAcabadasVO partida, ArrayList<HaJugadoVO> participantes) {
		this.partida=partida;
		this.participantes=participantes;
	}

	public PartidasAcabadasVO getPartida() {
		return partida;
	}

	public void setPartida(PartidasAcabadasVO partida) {
		this.partida = partida;
	}

	public ArrayList<HaJugadoVO> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(ArrayList<HaJugadoVO> participantes) {
		this.participantes = participantes;
	}
	
}
