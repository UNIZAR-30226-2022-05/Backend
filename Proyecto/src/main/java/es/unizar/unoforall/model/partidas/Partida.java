package es.unizar.unoforall.model.partidas;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.unizar.unoforall.model.salas.ConfigSala;

public class Partida {
	private boolean hayError;
	private String error;
	
	private List<Carta> mazo;
	private List<Carta> cartasJugadas;
	
	private List<Jugador> jugadores;
	private int turno;
	private boolean sentidoHorario;
	
	private ConfigSala configuracion;
	private boolean terminada;	
	
	//Fecha de inicio de la partida (Ya en formato sql porque no la necesita el frontend en este punto). 
	private Date fechaInicio; 
	private Carta.Color colorActual;
	private boolean esCambioDeColor;
	
	
	//Variables para extraer resultados de efectos
	private Carta vistaPorRayosX;
	private boolean efectoRayosX;
	private boolean modoAcumulandoRobo;
	private int roboAcumulado;
	private boolean modoJugarCartaRobada;
	private Carta cartaRobada;
	
	private static final int MAX_ROBO_ATTACK = 10;
	
	public Partida(String error) {	//Para construir una partida con error = true
		this.setHayError(true); 
		this.setError(error);
	}
	
	private class PosiblesTiposJugadas {
		public boolean esEscalera;
		public boolean esIguales;
		public boolean valida;
		
		public PosiblesTiposJugadas(boolean esEscalera, boolean esIguales, boolean valida) {
			this.esEscalera = esEscalera;
			this.esIguales = esIguales;
			this.valida = valida;
		}
	}
		
	public Partida(List<UUID> jugadoresID, ConfigSala configuracion) {
		this.setHayError(false);
		this.setError("");
		this.turno = 0;
		this.sentidoHorario = true;
		this.configuracion = configuracion;
		this.terminada = false;
				
				
		//Marcamos fecha de inicio
		fechaInicio = new Date(System.currentTimeMillis()); //Fecha actual.
		
		//Mazo
		this.mazo = new LinkedList<>();
		for(Carta.Color color : Carta.Color.values()) {
			if (color != Carta.Color.comodin) {
				for(Carta.Tipo tipo : Carta.Tipo.values()) {
					if (tipo.equals(Carta.Tipo.n0)) {
						this.mazo.add(new Carta(tipo,color));
					} else if (!tipo.equals(Carta.Tipo.cambioColor) &&
								!tipo.equals(Carta.Tipo.mas4) &&
								compruebaIncluirMazo(tipo)) {	//dos veces
						this.mazo.add(new Carta(tipo,color));
						this.mazo.add(new Carta(tipo,color));
					}
				}
			} else {
				for(int i = 0; i < 4; i++) {
					this.mazo.add(new Carta(Carta.Tipo.cambioColor,Carta.Color.comodin));
					this.mazo.add(new Carta(Carta.Tipo.mas4,Carta.Color.comodin));
				}
			}
		}
		//Collections.shuffle(this.mazo);
		
		
		// Cartas jugadas
		this.cartasJugadas = new LinkedList<>();
		this.cartasJugadas.add(getCartaInicial());
		
		
		// Jugadores
		this.jugadores = new LinkedList<>();
		for(UUID jID : jugadoresID) {
			this.jugadores.add(new Jugador(jID));
		}
			// Se crean las IA
		for(int i = 0; i < getNumIAs(); i++) {
			this.jugadores.add(new Jugador());
		}
			// Se crean las manos de todos los jugadores
		for(Jugador j : this.jugadores) {
			for (int i = 0; i < 7; i++) {
				j.getMano().add(robarCarta());
			}
		}
		
		
		
	}


	/**************************************************************************/
	// Funciones privadas
	/**************************************************************************/
	
	// Devuelve la primera carta de la partida
	private Carta getCartaInicial() {
		int indice = 0;
		Carta carta = this.mazo.get(indice);
		while(carta.getTipo()==Carta.Tipo.cambioColor 
				|| carta.getTipo()==Carta.Tipo.mas2 
				|| carta.getTipo()==Carta.Tipo.mas4 
				|| carta.getTipo()==Carta.Tipo.x2 
				|| carta.getTipo()==Carta.Tipo.rayosX 
				|| carta.getTipo()==Carta.Tipo.salta 
				|| carta.getTipo()==Carta.Tipo.intercambio 
				|| carta.getTipo()==Carta.Tipo.reversa) {
			indice++;
			carta = this.mazo.get(indice);
		}
		this.mazo.remove(indice);
		colorActual = carta.getColor();
		return carta;
	}
	
	// Comprueba si una carta especial debe incluirse en el mazo o no según las
	// reglas
	private boolean compruebaIncluirMazo(Carta.Tipo tipo) {
		if (tipo == Carta.Tipo.rayosX && !configuracion.getReglas().isCartaRayosX()) {
			return false;
		} else if (tipo == Carta.Tipo.intercambio && !configuracion.getReglas().isCartaIntercambio()) {
			return false;
		} else if (tipo == Carta.Tipo.x2 && !configuracion.getReglas().isCartaX2()){
			return false;
		} else {
			return true;
		}
	}
	
	private void avanzarTurno() {
		if (this.sentidoHorario) {
			this.turno++;
			if (this.turno >= this.jugadores.size()) {
				this.turno = 0;
			}
		} else {
			this.turno--;
			if (this.turno < 0) {
				this.turno = this.jugadores.size() - 1;
			}
		}
	}
	
	private Jugador siguienteJugador() {
		if (this.sentidoHorario) {
			if (this.turno == this.jugadores.size() - 1) {
				return this.jugadores.get(0);
			} else {
				return this.jugadores.get(this.turno + 1);
			}
		} else {
			if (this.turno == 0) {
				return this.jugadores.get(this.jugadores.size() - 1);
			} else {
				return this.jugadores.get(this.turno - 1);
			}
		}
	}
	
	private Jugador anteriorJugador() {
		if (!this.sentidoHorario) {
			if (this.turno == this.jugadores.size() - 1) {
				return this.jugadores.get(0);
			} else {
				return this.jugadores.get(this.turno + 1);
			}
		} else {
			if (this.turno == 0) {
				return this.jugadores.get(this.jugadores.size() - 1);
			} else {
				return this.jugadores.get(this.turno - 1);
			}
		}
	}
	
	private Carta robarCarta() {
		if (this.mazo.isEmpty()) {
			this.mazo.addAll(this.cartasJugadas);
			this.mazo.remove(0);
			Collections.shuffle(this.mazo);
			
			Carta ultimaCarta = this.cartasJugadas.get(0);
			this.cartasJugadas.clear();
			this.cartasJugadas.add(ultimaCarta);
		}
		Carta c = this.mazo.get(0);
		this.mazo.remove(0);
		return c;
	}
	
	
	private boolean compatibleAcumulador(Carta c) {
		if ((configuracion.getReglas().isEncadenarRoboCartas() 
				&& (c.getTipo().equals(Carta.Tipo.mas4) || c.getTipo().equals(Carta.Tipo.mas2))) 
			|| 
			(configuracion.getReglas().isRedirigirRoboCartas() && c.getTipo().equals(Carta.Tipo.reversa)) ) {
			return true;
		} else {
			return false;
		}
	}
	
	private PosiblesTiposJugadas evaluaJugada(Carta c1, Carta c2) {
		PosiblesTiposJugadas pj = null;
		if (c1.getTipo().equals(c2.getTipo())) {
			pj = new PosiblesTiposJugadas(false,true,true);
		} else if(c1.getTipo().ordinal()==c2.getTipo().ordinal()-1 || c1.getTipo().equals(Carta.Tipo.n9) && c2.getTipo().equals(Carta.Tipo.n0)) {
			pj = new PosiblesTiposJugadas(true,false,true);
		} else {
			pj = new PosiblesTiposJugadas(false,false,false);
		}
		return pj;
	}
<<<<<<< Updated upstream
	
	private Carta robarCarta() {
		if (this.mazo.isEmpty()) {
			while(this.cartasJugadas.size()!=1) {
				this.mazo.add(this.cartasJugadas.get(0));
				this.cartasJugadas.remove(0);
			}
			Collections.shuffle(this.mazo);
		}
		Carta c = this.mazo.get(0);
		this.mazo.remove(0);
		if (this.mazo.isEmpty()) {
			while(this.cartasJugadas.size()!=1) {
				this.mazo.add(this.cartasJugadas.get(0));
				this.cartasJugadas.remove(0);
			}
			Collections.shuffle(this.mazo);
		}
		return c;
	}
	
=======
>>>>>>> Stashed changes


	/**************************************************************************/
	// Funciones públicas
	/**************************************************************************/
	
	public void ejecutarJugada(Jugada jugada) {
		if(modoJugarCartaRobada) {
			if(jugada.getCartas()!=null && jugada.getCartas().size()==1) {
				juegaCarta(cartaRobada, jugada);
			}
			cartaRobada=null;
			modoJugarCartaRobada=false;
		} else if(jugada.robar) {
			if(modoAcumulandoRobo) {
				modoAcumulandoRobo=false;
				for(int i = 0; i<roboAcumulado; i++) {
					this.jugadores.get(turno).getMano().add(robarCarta());
				}
				roboAcumulado=0;
			} else if (configuracion.getModoJuego().equals(ConfigSala.ModoJuego.Attack)) {
					int random_robo = (int)Math.floor(Math.random()*(MAX_ROBO_ATTACK)+1);
					for (int i = 0; i < random_robo; i++) {
						this.jugadores.get(turno).getMano().add(robarCarta());
					}
			} else {
				Carta cartaRobada = robarCarta();
				this.jugadores.get(turno).getMano().add(cartaRobada);
				if (cartaRobada.getColor().equals(colorActual) || cartaRobada.getColor().equals(Carta.Color.comodin) 
						|| cartaRobada.getTipo().equals(getUltimaCartaJugada().getTipo())) {
					modoJugarCartaRobada=true;
				}
			}
			
		} else {
			for (Carta c : jugada.cartas) {
				juegaCarta(c, jugada);
			}
			if (!esCambioDeColor) {
				colorActual = getUltimaCartaJugada().getColor();
			}
			
		}
		
		if(!modoJugarCartaRobada) {
			avanzarTurno();
		}
		
		// Se comprueba si se ha acabado la partida
		for (Jugador j : this.jugadores) {
			if (j.getMano().size() == 0) {
				this.terminada = true;
			}
		}
		
		
		//eventos asíncronos: emojis, botón de UNO, tiempo, votación pausa
	}
	
	public void ejecutarJugadaJugador(Jugada jugada, UUID jugadorID) {
		if (validarJugada(jugada) && 
				this.jugadores.get(turno).getJugadorID().equals(jugadorID)) {
			ejecutarJugada(jugada);
		}
	}
	
	public void ejecutarJugadaIA() {
		if (this.jugadores.get(turno).isEsIA()) {
			Jugada jugadaIA = new Jugada();
			//TODO crear jugada según modo de juego y cartas jugables. IA -> Después de uno original funcional.
			
			ejecutarJugada(jugadaIA);
		}
	}
	
	public boolean turnoDeIA() {
		return this.jugadores.get(turno).isEsIA();
	}
	
	public void expulsarJugador(UUID jugadorID) {
		//se sustituye por IA
		for (Jugador j : jugadores) {
			if(j.getJugadorID().equals(jugadorID)) {
				j.setEsIA(true);
				j.setJugadorID(null);
				break;
			}
		}
	}
	
	public void pulsarBotonUNO(UUID jugador) {
		for (Jugador j : this.jugadores) {
			if (j.getJugadorID().equals(jugador)) {
				j.setProtegido_UNO(true);
			} else if(!j.isProtegido_UNO() && j.getMano().size()==1) { //Pillado, roba dos cartas.
				this.jugadores.get(turno).getMano().add(robarCarta());
				this.jugadores.get(turno).getMano().add(robarCarta());
			}
				
		}
	}
	
	public int getNumIAs() {
		return configuracion.getMaxParticipantes() - this.jugadores.size();
	}
	
	/**************************************************************************/
	// Para los FRONTENDs
	/**************************************************************************/
	
	// Se devuelven ordenados en sentido horario
	public List<Jugador> getJugadores() {
		return jugadores;
	}
	
	public UUID getIDJugadorActual() {
		return this.jugadores.get(this.turno).getJugadorID();
	}
	
	public Carta getUltimaCartaJugada() {
		return this.cartasJugadas.get(this.cartasJugadas.size()-1);
	}
	
	public boolean validarJugada(Jugada jugada) {
		if (jugada.robar) {
			if(jugadores.get(turno).getMano().size()>=20 && !compruebaPuedeJugar()) {
				return false;
			} else {
				return true;
			}
		} else if(modoJugarCartaRobada) {
			if(jugada.getCartas().isEmpty() || jugada.getCartas().get(0).equals(cartaRobada)) {
				return true;
			} else {
				return false;
			}
		}else if (jugada.cartas == null || jugada.cartas.isEmpty()) {
			return false;
		} else if(modoAcumulandoRobo) {
			Carta anterior = getUltimaCartaJugada();
			if(jugada.getCartas().size()!=1) { //Solo se puede jugar una
				return false;
			} else {
				Carta c = jugada.getCartas().get(0);
				if(compatibleAcumulador(c) && 
						(c.getTipo().equals(anterior.getTipo()) //Si la carta es usable según las reglas
								|| c.getColor().equals(colorActual)  
								|| c.getTipo().equals(Carta.Tipo.mas4))) {
					return true;
				}
			}
	    } else {
			Carta anterior = getUltimaCartaJugada();
			boolean valida = false;
			Carta.Tipo tipo = jugada.getCartas().get(0).getTipo();
			
			//Las únicas cartas que hacen "jugadas" son los números, para el resto de cartas solo se puede jugar una.
			if(configuracion.getReglas().isJugarVariasCartas() && (tipo.equals(Carta.Tipo.n0) || tipo.equals(Carta.Tipo.n1) 
					|| tipo.equals(Carta.Tipo.n2) || tipo.equals(Carta.Tipo.n3) || tipo.equals(Carta.Tipo.n4) || tipo.equals(Carta.Tipo.n5) 
					|| tipo.equals(Carta.Tipo.n6) || tipo.equals(Carta.Tipo.n7) || tipo.equals(Carta.Tipo.n9))) {
				int numCartas = 0; //Se necesitan dos para definir si son escaleras o iguales
				PosiblesTiposJugadas pj = new PosiblesTiposJugadas(false,false,false);
				for (Carta c : jugada.cartas) {
					if (numCartas <= 1) {
						if(numCartas==0) {
							valida = c.getTipo().equals(anterior.getTipo()) || c.getColor().equals(colorActual);
							anterior = c;
						} else {
							pj = evaluaJugada(anterior,c);
							valida = pj.valida;
						}
						numCartas++;
					} else {
						if(pj.esEscalera) {
							valida = anterior.getTipo().ordinal()==c.getTipo().ordinal()-1 || 
									anterior.getTipo().equals(Carta.Tipo.n9) && c.getTipo().equals(Carta.Tipo.n0);
						} else if(pj.esIguales){
							valida = c.getTipo().equals(anterior.getTipo());
						} else {
							valida = false;
						}
					}
					if(!valida) {
						break;
					}
				}
			} else { //Cartas con efecto o en general sin poder jugar varias cartas
				if (jugada.cartas.size()>1) {
					valida = false; //Solo se puede jugar una si no son números. (o si no se permite jugar más de una).
				}
				return jugada.getCartas().get(0).getTipo().equals(anterior.getTipo()) 
						|| jugada.getCartas().get(0).getColor().equals(colorActual);
			}
			
			return valida;
		}
		return false;
	}
	
	// Se debe mirar en cada turno, y cuando devuelva true ya se puede desconectar
	// del buzón de la partida con websockets
	public boolean estaTerminada() {
		return this.terminada;
	}
	
	public boolean isHayError() {
		return hayError;
	}

	public void setHayError(boolean hayError) {
		this.hayError = hayError;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public ConfigSala getConfiguracion() {
		return configuracion;
	}
	
	/**
	 * @return			null si no se ha jugado una carta de rayosX el turno anterior o no eres quien la jugó.
	 * 					la carta vista si se ha jugado por el jugador.
	 */
	public Carta getRayosX(UUID jugadorID) {
		if(efectoRayosX && jugadorID.equals(anteriorJugador().getJugadorID())) {
			//Si el jugador ha jugado una rayosX en el turno anterior
			return vistaPorRayosX; 
		}
		return null;
	}
	
	
	
	
	@Override
	public String toString() {
		return "Partida [hayError=" + hayError + ", error=" + error + "\n\t mazo=" + mazo + "\n\n\t cartasJugadas="
				+ cartasJugadas + "\n\n\t jugadores=" + jugadores + ", turno=" + turno + ", sentidoHorario=" + sentidoHorario
				+ ", configuracion=" + configuracion + ", terminada=" + terminada + ", fechaInicio=" + fechaInicio
				+ ", colorActual=" + colorActual + ", esCambioDeColor=" + esCambioDeColor + ", vistaPorRayosX="
				+ vistaPorRayosX + ", efectoRayosX=" + efectoRayosX + ", modoAcumulandoRobo=" + modoAcumulandoRobo
				+ ", roboAcumulado=" + roboAcumulado + "]";
	}
	
}
