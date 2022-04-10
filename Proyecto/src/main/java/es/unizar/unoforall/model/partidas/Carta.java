package es.unizar.unoforall.model.partidas;


public class Carta {
	public enum Tipo {n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, mas2, reversa, salta, rayosX, intercambio, x2, cambioColor, mas4};
	public enum Color {rojo, amarillo, azul, verde, comodin};
	
	private Tipo tipo;
	private Color color;
	
	
	public Carta(Tipo tipo, Color color) {
		super();
		this.tipo = tipo;
		this.color = color;
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
	
	
}
