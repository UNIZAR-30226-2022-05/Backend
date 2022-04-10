package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Jugador {
	private boolean esIA;
	private UUID jugadorID;
	private List<Carta> mano;
	
	// Para crear un jugador IA
	public Jugador() {
		this.esIA = true;
		this.jugadorID = null;
		this.mano = new ArrayList<>();
	}
	
	// Para crear un jugador real
	public Jugador(UUID jugadorID) {
		this.esIA = false;
		this.jugadorID = jugadorID;
		this.mano = new ArrayList<>();
	}

	public boolean isEsIA() {
		return esIA;
	}

	public void setEsIA(boolean esIA) {
		this.esIA = esIA;
	}

	public UUID getJugadorID() {
		return jugadorID;
	}

	public void setJugadorID(UUID jugadorID) {
		this.jugadorID = jugadorID;
	}

	public List<Carta> getMano() {
		return mano;
	}

	public void setMano(List<Carta> mano) {
		this.mano = mano;
	}
}