package es.unizar.unoforall.model.partidas;

import java.util.List;

public class Jugada {
	List<Carta> cartas;		//En el orden en el que se quieren tirar
	boolean robar;
	Carta.Color nuevoColor;
	
	// La jugada es robar
	public Jugada() {
		this.cartas = null;
		this.robar = true;
	}
	
	public Jugada(List<Carta> cartas) {
		this.cartas = cartas;
		this.robar = false;
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
		return "Jugada [cartas=" + cartas + ", robar=" + robar + ", nuevoColor=" + nuevoColor + "]";
	}
	
	
}
