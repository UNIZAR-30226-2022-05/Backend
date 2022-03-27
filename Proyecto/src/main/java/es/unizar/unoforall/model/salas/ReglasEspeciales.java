package es.unizar.unoforall.model.salas;

public class ReglasEspeciales {
	private boolean encadenarRoboCartas;	//Encadenar +2 y +4
	private boolean redirigirRoboCartas;	//Redirigir +2 y +4 con cambio de sentido
	private boolean jugarVariasCartas;		//Jugar varias cartas del mismo valor o en escalera
	private boolean evitarEspecialFinal;	//Se roban 2 cartas si la última es +4 o cambio de color
	
	private boolean cartaRayosX;			//Ver carta aleatoria del siguiente jugador
	private boolean cartaIntercambio;		//Intercambiar mano por la del siguiente jugador
	private boolean cartaX2;				//Hacer que el siguiente jugador robe el número de cartas que posee
	
	public ReglasEspeciales() {
		
	}
	
	public ReglasEspeciales(boolean encadenarRoboCartas, boolean redirigirRoboCartas, boolean jugarVariasCartas,
			boolean evitarEspecialFinal, boolean cartaRayosX, boolean cartaIntercambio, boolean cartaX2) {
		super();
		this.encadenarRoboCartas = encadenarRoboCartas;
		this.redirigirRoboCartas = redirigirRoboCartas;
		this.jugarVariasCartas = jugarVariasCartas;
		this.evitarEspecialFinal = evitarEspecialFinal;
		this.cartaRayosX = cartaRayosX;
		this.cartaIntercambio = cartaIntercambio;
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
	
	
}