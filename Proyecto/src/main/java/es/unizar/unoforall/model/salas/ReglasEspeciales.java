package es.unizar.unoforall.model.salas;

import java.util.Objects;

public class ReglasEspeciales {
	private boolean encadenarRoboCartas;	//Encadenar +2 y +4
	private boolean redirigirRoboCartas;	//Redirigir +2 y +4 con cambio de sentido
	private boolean jugarVariasCartas;		//Jugar varias cartas del mismo valor o en escalera
	private boolean evitarEspecialFinal;	//Se roban 2 cartas si la última es +4 o cambio de color
	
	private boolean cartaRayosX;			//Ver carta aleatoria del siguiente jugador
	private boolean cartaIntercambio;		//Intercambiar mano por la del siguiente jugador
	private boolean cartaX2;				//Hacer que el siguiente jugador robe el número de cartas que posee
	
	private boolean reglasValidas;			//Indica si se van a especificar o se van a dejar indefinidas
	
	public ReglasEspeciales() {
		this.encadenarRoboCartas = false;
		this.redirigirRoboCartas = false;
		this.jugarVariasCartas = false;
		this.evitarEspecialFinal = false;
		this.cartaRayosX = false;
		this.cartaIntercambio = false;
		this.cartaX2 = false;
		this.setReglasValidas(false);
	}

	public ReglasEspeciales(boolean encadenarRoboCartas, boolean redirigirRoboCartas, boolean jugarVariasCartas,
			boolean evitarEspecialFinal, boolean cartaRayosX, boolean cartaIntercambio, boolean cartaX2) {
		this.encadenarRoboCartas = encadenarRoboCartas;
		this.redirigirRoboCartas = redirigirRoboCartas;
		this.jugarVariasCartas = jugarVariasCartas;
		this.evitarEspecialFinal = evitarEspecialFinal;
		this.cartaRayosX = cartaRayosX;
		this.cartaIntercambio = cartaIntercambio;
		this.cartaX2 = cartaX2;
	}
	
	public void setEncadenarRoboCartas(boolean encadenarRoboCartas) {
		this.encadenarRoboCartas = encadenarRoboCartas;
	}

	public void setRedirigirRoboCartas(boolean redirigirRoboCartas) {
		this.redirigirRoboCartas = redirigirRoboCartas;
	}

	public void setJugarVariasCartas(boolean jugarVariasCartas) {
		this.jugarVariasCartas = jugarVariasCartas;
	}

	public void setEvitarEspecialFinal(boolean evitarEspecialFinal) {
		this.evitarEspecialFinal = evitarEspecialFinal;
	}

	public void setCartaRayosX(boolean cartaRayosX) {
		this.cartaRayosX = cartaRayosX;
	}

	public void setCartaIntercambio(boolean cartaIntercambio) {
		this.cartaIntercambio = cartaIntercambio;
	}

	public void setCartaX2(boolean cartaX2) {
		this.cartaX2 = cartaX2;
	}

	public boolean isEncadenarRoboCartas() {
		return encadenarRoboCartas;
	}

	public boolean isRedirigirRoboCartas() {
		return redirigirRoboCartas;
	}

	public boolean isJugarVariasCartas() {
		return jugarVariasCartas;
	}

	public boolean isEvitarEspecialFinal() {
		return evitarEspecialFinal;
	}

	public boolean isCartaRayosX() {
		return cartaRayosX;
	}

	public boolean isCartaIntercambio() {
		return cartaIntercambio;
	}

	public boolean isCartaX2() {
		return cartaX2;
	}

	@Override
	public String toString() {
		return "ReglasEspeciales [encadenarRoboCartas=" + encadenarRoboCartas + ", redirigirRoboCartas="
				+ redirigirRoboCartas + ", jugarVariasCartas=" + jugarVariasCartas + ", evitarEspecialFinal="
				+ evitarEspecialFinal + ", cartaRayosX=" + cartaRayosX + ", cartaIntercambio=" + cartaIntercambio
				+ ", cartaX2=" + cartaX2 + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cartaIntercambio, cartaRayosX, cartaX2, encadenarRoboCartas, evitarEspecialFinal,
				jugarVariasCartas, redirigirRoboCartas);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReglasEspeciales other = (ReglasEspeciales) obj;
		return cartaIntercambio == other.cartaIntercambio && cartaRayosX == other.cartaRayosX
				&& cartaX2 == other.cartaX2 && encadenarRoboCartas == other.encadenarRoboCartas
				&& evitarEspecialFinal == other.evitarEspecialFinal && jugarVariasCartas == other.jugarVariasCartas
				&& redirigirRoboCartas == other.redirigirRoboCartas;
	}

	public boolean isReglasValidas() {
		return reglasValidas;
	}

	public void setReglasValidas(boolean reglasValidas) {
		this.reglasValidas = reglasValidas;
	}
	
	@Override
	public ReglasEspeciales clone() {
		ReglasEspeciales copy = new ReglasEspeciales();
		copy.encadenarRoboCartas = this.encadenarRoboCartas;
		copy.redirigirRoboCartas = this.redirigirRoboCartas;
		copy.jugarVariasCartas = this.jugarVariasCartas;
		copy.evitarEspecialFinal = this.evitarEspecialFinal;
		copy.cartaRayosX = this.cartaRayosX;
		copy.cartaIntercambio = this.cartaIntercambio;
		copy.cartaX2 = this.cartaX2;

		return copy;
	}
    
}