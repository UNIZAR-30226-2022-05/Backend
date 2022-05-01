package es.unizar.unoforall.model.partidas;

public class RespuestaVotacionPausa {
	int numVotos;
	int numVotantes;
	
	public RespuestaVotacionPausa(int numVotos, int numVotantes) {
		super();
		this.numVotos = numVotos;
		this.numVotantes = numVotantes;
	}
	
	public int getNumVotos() {
		return numVotos;
	}
	public int getNumVotantes() {
		return numVotantes;
	}
}
