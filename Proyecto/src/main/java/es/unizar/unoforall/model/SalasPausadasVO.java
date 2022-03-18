package es.unizar.unoforall.model;

import java.util.UUID;

import es.unizar.unoforall.Sala;

public class SalasPausadasVO {
	private final UUID id;
	private int maxParticipantes;
	private Sala sala;
	
	public SalasPausadasVO(UUID id, int maxParticipantes, Sala sala) {
		super();
		this.id = id;
		this.maxParticipantes = maxParticipantes;
		this.sala = sala;
	}

	public int getMaxParticipantes() {
		return maxParticipantes;
	}

	public void setMaxParticipantes(int maxParticipantes) {
		this.maxParticipantes = maxParticipantes;
	}

	public Sala getSala() {
		return sala;
	}

	public void setSala(Sala sala) {
		this.sala = sala;
	}

	public UUID getId() {
		return id;
	}
}
