package es.unizar.unoforall.model.partidas;

import java.util.List;

public class Jugada {
	private List<Carta> cartas;		//En el orden en el que se quieren tirar
	private boolean robar;
	private Carta.Color nuevoColor;
	private int jugadorObjetivo;
	
	// La jugada es robar
	public Jugada() {
		this.cartas = null;
		this.robar = true;
		this.nuevoColor = Carta.Color.comodin;	//por ejemplo (no se va a usar)
	}
	
	public Jugada(List<Carta> cartas) {
		this.cartas = cartas;
		this.robar = false;
		this.nuevoColor = cartas.get(cartas.size()-1).getColor();
	}
	
	// Utilizar si la última carta es un cambio de color
	public Jugada(List<Carta> cartas, Carta.Color nuevoColor) {
		this.cartas = cartas;
		this.robar = false;
		this.nuevoColor = nuevoColor;
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

	public Carta.Color getNuevoColor() {
		return nuevoColor;
	}

	public void setNuevoColor(Carta.Color nuevoColor) {
		this.nuevoColor = nuevoColor;
	}

	@Override
	public String toString() {
		return "Jugada [cartas=" + cartas + ", robar=" + robar + ", nuevoColor=" + nuevoColor + "]";
	}
	
	
}
