package es.unizar.unoforall.model.partidas;


public class Carta implements Comparable<Carta> {
	public enum Tipo {n0(10), n1(1), n2(2), n3(3), n4(4), n5(5), n6(6), n7(7), n8(8), n9(9), mas2(11), reversa(12), salta(13), rayosX(14), intercambio(15), x2(16), cambioColor(17), mas4(18); 
					private int valor;			
					private Tipo(int valor) {
						this.valor = valor;
					}};
	public enum Color {rojo, amarillo, azul, verde, comodin};
	
	private Tipo tipo;
	private Color color;
	
	
	public Carta(Tipo tipo, Color color) {
		super();
		this.tipo = tipo;
		this.color = color;
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
}
