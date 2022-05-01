package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;

import es.unizar.unoforall.model.PartidasAcabadasVO;

public class PartidaJugada {

	private PartidasAcabadasVO partida;
	private ArrayList<Participante> participantes = null;
	
	public PartidaJugada (PartidasAcabadasVO partida, ArrayList<Participante> participantes) {
		this.partida=partida;
		this.participantes=participantes;
	}

	public PartidasAcabadasVO getPartida() {
		return partida;
	}

	public void setPartida(PartidasAcabadasVO partida) {
		this.partida = partida;
	}

	public ArrayList<Participante> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
	}
	
}
