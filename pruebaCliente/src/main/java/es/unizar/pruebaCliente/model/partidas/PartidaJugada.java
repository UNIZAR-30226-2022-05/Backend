package es.unizar.pruebaCliente.model.partidas;

import java.util.ArrayList;

import es.unizar.pruebaCliente.model.PartidasAcabadasVO;


public class PartidaJugada {

	private PartidasAcabadasVO partida;
	private ArrayList<HaJugadoVO> participantes = null;
	
	public PartidaJugada (PartidasAcabadasVO partida, ArrayList<HaJugadoVO> participantes) {
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
