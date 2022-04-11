package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Jugador {
	private boolean esIA;
	private UUID jugadorID;
	private List<Carta> mano;
	private boolean protegido_UNO;
	
	// Para crear un jugador IA
	public Jugador() {
		this.esIA = true;
		this.jugadorID = null;
		this.mano = new ArrayList<>();
		this.protegido_UNO = false;
	}
	
	// Para crear un jugador real
	public Jugador(UUID jugadorID) {
		this.esIA = false;
		this.jugadorID = jugadorID;
		this.mano = new ArrayList<>();
		this.protegido_UNO = false;
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

	public boolean isProtegido_UNO() {
		return protegido_UNO;
	}

	public void setProtegido_UNO(boolean protegido_UNO) {
		this.protegido_UNO = protegido_UNO;
	}
	
	//Saca los puntos correspondientes a la mano actual
	public int sacarPuntos() {
		int puntuacion = 0;
		for (Carta c : mano) {  //Caso cartas numéricas
			if(c.getTipo().ordinal()>=0 && c.getTipo().ordinal()<10) {
				puntuacion += c.getTipo().ordinal();
			} else { 			//Caso cartas especiales
				puntuacion += 10;
			}
		}
		return puntuacion;
	}
}