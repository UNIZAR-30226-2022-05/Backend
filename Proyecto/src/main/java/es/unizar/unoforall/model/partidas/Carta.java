package es.unizar.unoforall.model.partidas;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Carta implements Comparable<Carta> {
	
	public enum Tipo {n0(10), n1(1), n2(2), n3(3), n4(4), n5(5), n6(6), n7(7), n8(8), n9(9), mas2(11), reversa(12), salta(13), rayosX(14), intercambio(15), x2(16), cambioColor(17), mas4(18); 
					public int valor;			
					private Tipo(int valor) {
						this.valor = valor;
					}};
	public enum Color {rojo, amarillo, azul, verde, comodin};
	
	private Tipo tipo;
	private Color color;
	private HashMap<Integer,Boolean> visiblePor;
	
	public Carta(Tipo tipo, Color color) {
		super();
		this.tipo = tipo;
		this.color = color;
		this.visiblePor = new HashMap<Integer,Boolean>();
		for (int i = 0; i < 4 ; i++) {
			visiblePor.put(i,false);
		}
	}
	
//	public Carta(Tipo tipo, Color color, int numJugadores) {
//		super();
//		this.tipo = tipo;
//		this.color = color;
//		this.visiblePor = new HashMap<Integer,Boolean>();
//		for (int i = 0; i < numJugadores ; i++) {
//			visiblePor.put(i,false);
//		}
//	}

	public boolean isVisiblePor(int jugador) {
		if(visiblePor.containsKey(jugador)) {
			return visiblePor.get(jugador);
		}
		return false;
	}
	
	public void setOculta() {
		for (int i = 0; i < visiblePor.size(); i++ ) {
			visiblePor.put(i,false);
		}
	}
	
	public boolean marcarVisible(int jugador) {
		if(visiblePor.containsKey(jugador)) {
			visiblePor.put(jugador, true);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean esCompatible(Carta carta) {
		if (this.color == Color.comodin || carta.color == Color.comodin) {
			return true;
		} else if (this.color == carta.color || this.tipo == carta.tipo) {
			return true;
		} else {			
			return false;
		}
	}
	
	public boolean esDelColor(Color color) {
		return this.color==color;
	}
	
	public boolean esDelTipo(Tipo tipo) {
		return this.tipo==tipo;
	}
	
	public static boolean esNumero(Tipo tipo) {
		if(tipo.ordinal()<10) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean compartenTipo(Carta c1, Carta c2) {
		boolean respuesta = c1.getTipo()==c2.getTipo();
		return respuesta;
	}
	
	public static boolean compartenColor(Carta c1, Carta c2) {
		boolean respuesta = c1.getColor()==c2.getColor();
		return respuesta;
	}
	
	//Solo para las numéricas
	public static boolean sonConsecutivas(Carta c1, Carta c2) {
		return compartenColor(c1,c2) &&
				(c1.getTipo().ordinal()==c2.getTipo().ordinal()-1 || 
				c1.getTipo().equals(Carta.Tipo.n9) && c2.getTipo().equals(Carta.Tipo.n0));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(color, tipo);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Carta other = (Carta) obj;
		return color == other.color && tipo == other.tipo;
	}


	public Tipo getTipo() {
		return tipo;
	}


	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}


	public Color getColor() {
		return color;
	}


	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public int compareTo(Carta carta) {
		int result = this.color.compareTo(carta.color);
		if (result == 0) {
			return this.tipo.compareTo(carta.tipo);
		} else {
			return result;
		}
	}
	
	@Override
	public String toString() {
		return "Carta [tipo=" + tipo + ", color=" + color + "]\n";
	}
}
