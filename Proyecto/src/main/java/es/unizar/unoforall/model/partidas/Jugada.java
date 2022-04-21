package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;
import java.util.List;

public class Jugada {
	private List<Carta> cartas;		//En el orden en el que se quieren tirar
	private boolean robar;
	private int jugadorObjetivo;
	
	// La jugada es robar
	public Jugada() {
		this.cartas = new ArrayList<>();;
		this.robar = true;
	}
	
	public Jugada(ArrayList<Carta> cartas) {
		this.cartas = cartas;
		this.robar = false;
	}
	
	// Utilizar si la última carta es un cambio de color
	public Jugada(ArrayList<Carta> cartas, Carta.Color nuevoColor) {
		this.cartas = cartas;
		this.robar = false;
	}
	
	public int getJugadorObjetivo() {
		return jugadorObjetivo;
	}

	public void setJugadorObjetivo(int jugadorObjetivo) {
		this.jugadorObjetivo = jugadorObjetivo;
	}

	public List<Carta> getCartas() {
		return cartas;
	}

	public void setCartas(List<Carta> cartas) {
		this.cartas = cartas;
	}

	public boolean isRobar() {
		return robar;
	}

	public void setRobar(boolean robar) {
		this.robar = robar;
	}


	@Override
	public String toString() {
		return "Jugada [cartas=" + cartas + ", robar=" + robar + "]";
	}
	
	
}
