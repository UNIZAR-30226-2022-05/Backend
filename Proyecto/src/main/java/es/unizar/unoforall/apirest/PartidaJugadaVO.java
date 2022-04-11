package es.unizar.unoforall.apirest;

import java.util.ArrayList;

import es.unizar.unoforall.model.HaJugadoVO;
import es.unizar.unoforall.model.PartidasAcabadasVO;

public class PartidaJugadaVO {

	private PartidasAcabadasVO partida;
	private ArrayList<HaJugadoVO> participantes = null;
	
	public PartidaJugadaVO (PartidasAcabadasVO partida, ArrayList<HaJugadoVO> participantes) {
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
