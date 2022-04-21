package es.unizar.unoforall.model.partidas;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import es.unizar.unoforall.model.salas.ConfigSala;

public class Partida {
	private boolean hayError = false;
	private String error = "";
	
	private List<Carta> mazo = null;
	private List<Carta> cartasJugadas = null;
	
	private List<Jugador> jugadores = null;
	private int turno = 0;
	private boolean sentidoHorario = true;
	
	private ConfigSala configuracion = null;
	private boolean terminada = false;	
	
	//Fecha de inicio de la partida (Ya en formato sql porque no la necesita el frontend en este punto). 
	private Date fechaInicio = null; 
	
	
	//Variables para extraer resultados de efectos
	private Carta vistaPorRayosX = null;
	private boolean efectoRayosX = false;
	private boolean modoAcumulandoRobo = false;
	private int roboAcumulado = 0;
	private boolean modoJugarCartaRobada = false;
	private Carta cartaRobada = null;
	
	private static final int MAX_ROBO_ATTACK = 10;
	
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
	
	public Partida(String error) {	//Para construir una partida con error = true
		this.setHayError(true);
		this.setError(error);
	}
	
	private Partida() {
		
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
					} else if (compruebaIncluirMazo(tipo)) {	//dos veces
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
		Collections.shuffle(this.mazo); 
		
		
		// Cartas jugadas
		this.cartasJugadas = new LinkedList<>();
		this.cartasJugadas.add(getCartaInicial());
		
		// Jugadores
		this.jugadores = new LinkedList<>();
		for(UUID jID : jugadoresID) {
			this.jugadores.add(new Jugador(jID));
		}
			// Se crean las IA
		int numIAs = configuracion.getMaxParticipantes() - jugadoresID.size();
		for(int i = 0; i < numIAs; i++) {
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
	//FUNCIONA
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
		return carta;
	}
	
	//FUNCIONA
	// Comprueba si una carta especial debe incluirse en el mazo o no según las
	// reglas
	private boolean compruebaIncluirMazo(Carta.Tipo tipo) {
		if (tipo == Carta.Tipo.rayosX && !configuracion.getReglas().isCartaRayosX()) {
			return false;
		} else if (tipo == Carta.Tipo.intercambio && !configuracion.getReglas().isCartaIntercambio()) {
			return false;
		} else if (tipo == Carta.Tipo.x2 && !configuracion.getReglas().isCartaX2()){
			return false;
		} else if (!tipo.equals(Carta.Tipo.cambioColor) && !tipo.equals(Carta.Tipo.mas4)){
			return true;
		} else {
			return false;
		}
	}
	
	//FUNCIONA
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
	
	//FUNCIONA
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
	
	private boolean compatibleAcumulador(Carta c) {
		if ((configuracion.getReglas().isEncadenarRoboCartas() 
				&& (c.esDelTipo(Carta.Tipo.mas4) || c.esDelTipo(Carta.Tipo.mas2))) 
			|| 
			(configuracion.getReglas().isRedirigirRoboCartas() && c.esDelTipo(Carta.Tipo.reversa)) ) {
			return true;
		} else {
			return false;
		}
	}
	
	private PosiblesTiposJugadas evaluaJugada(Carta c1, Carta c2) {
		PosiblesTiposJugadas pj = null;
		if (Carta.compartenTipo(c1, c2)) {
			pj = new PosiblesTiposJugadas(false,true,true);
		} else if(Carta.sonConsecutivas(c1, c2)) {
			pj = new PosiblesTiposJugadas(true,false,true);
		} else {
			pj = new PosiblesTiposJugadas(false,false,false);
		}
		return pj;
	}
	
	private Carta robarCarta() {
		if (this.mazo.isEmpty()) {
			System.err.println("Intento de robo de mazo vacÃ­o.");
			Carta auxiliar = cartasJugadas.get(cartasJugadas.size()-1);
			cartasJugadas.remove(cartasJugadas.size()-1);
			this.mazo.addAll(cartasJugadas);
			cartasJugadas.removeAll(cartasJugadas);
			cartasJugadas.add(auxiliar);
			/*while(this.cartasJugadas.size()!=1) {
				this.mazo.add(this.cartasJugadas.get(0));
				this.cartasJugadas.remove(0);
			}*/
			Collections.shuffle(this.mazo);
		}
		Carta c = this.mazo.get(0);
		if (c.getTipo().equals(Carta.Tipo.cambioColor) || c.getTipo().equals(Carta.Tipo.mas4)) {
			c.setColor(Carta.Color.comodin);
		}
		
		this.mazo.remove(0);
		if (this.mazo.isEmpty()) {
			Carta auxiliar = cartasJugadas.get(cartasJugadas.size()-1);
			cartasJugadas.remove(cartasJugadas.size()-1);
			this.mazo.addAll(cartasJugadas);
			cartasJugadas.removeAll(cartasJugadas);
			cartasJugadas.add(auxiliar);
			/*while(this.cartasJugadas.size()!=1) {
				this.mazo.add(this.cartasJugadas.get(0));
				this.cartasJugadas.remove(0);
			}*/
			Collections.shuffle(this.mazo);
		}
		return c;
	}

	private void juegaCarta(Carta c, Jugada jugada) {
		c.setOculta();
		efectoRayosX = false;
		boolean esSalto = false;
		boolean hayIntercambio = false;
		boolean hayReversa = false;
		
		switch (c.getTipo()) {
			case intercambio:
				hayIntercambio = true;
				break;
				
			case mas2:
				if(configuracion.getReglas().isEncadenarRoboCartas() || configuracion.getReglas().isRedirigirRoboCartas()) {
					if(!modoAcumulandoRobo) {
						modoAcumulandoRobo = true;
						roboAcumulado = 2;
					} else {
						roboAcumulado+=2;
					}
				} else {
					for (int i = 0; i < 2; i++) {
						siguienteJugador().getMano().add(robarCarta());
					}
					esSalto=true;
				}
				break;
				
			case mas4:
				if(configuracion.getReglas().isEncadenarRoboCartas() || configuracion.getReglas().isRedirigirRoboCartas()) {
					if(!modoAcumulandoRobo) {
						modoAcumulandoRobo = true;
						roboAcumulado = 4;
					} else {
						roboAcumulado+=4;
					}
				} else {
					for (int i = 0; i < 4; i++) {
						siguienteJugador().getMano().add(robarCarta());
					}
					esSalto=true;
				}
				break;
				
			case x2:
				int numCartas = siguienteJugador().getMano().size();
				for (int i = 0; i < numCartas; i++) {
					if(siguienteJugador().getMano().size()==20) {
						break;
					}
					siguienteJugador().getMano().add(robarCarta());
				}
				esSalto=true;
				break;
				
			case rayosX:
				for (int i = 0 ; i < configuracion.getMaxParticipantes() ; i++) {
					if( i != turno ) {
						Jugador j = jugadores.get(i);
						List<Carta> mano = j.getMano();
						Collections.shuffle(mano);
						boolean hecho = false;
						int carta = 0;
						while(!hecho && carta<mano.size()) {
							if(!mano.get(carta).isVisiblePor(turno)) {
								mano.get(carta).marcarVisible(turno);
								hecho = true;
							}
						}
					}
				}
				List<Carta> mano = jugadores.get(jugada.getJugadorObjetivo()).getMano();
				Collections.shuffle(mano);
				vistaPorRayosX = mano.get(0);
				efectoRayosX = true;
				break;
				
			case reversa:
				this.sentidoHorario = ! this.sentidoHorario;
				hayReversa = true;
				break;
				
			case salta:
				esSalto = true;//avanzarTurno();
				break;
				
			case cambioColor:
				break;

			default:
				break;
		}
		this.cartasJugadas.add(c); //La añade al final (por implementaciones de rellenar y robar del mazo);
		this.jugadores.get(turno).getMano().remove(c);
		if(	configuracion.getReglas().isEvitarEspecialFinal() && 
				this.jugadores.get(turno).getMano().size()==1 &&
				this.jugadores.get(turno).getMano().get(0).esDelColor(Carta.Color.comodin)) {
			for (int i = 0; i < 2; i++) {
				this.jugadores.get(turno).getMano().add(robarCarta());
			}
		}
		if(hayIntercambio) {
			List<Carta> nuevaMano = new ArrayList<>(jugadores.get(jugada.getJugadorObjetivo()).getMano());
			jugadores.get(jugada.getJugadorObjetivo()).getMano().clear();
			jugadores.get(jugada.getJugadorObjetivo()).getMano().addAll(jugadores.get(turno).getMano());
			jugadores.get(turno).getMano().clear();
			jugadores.get(turno).getMano().addAll(nuevaMano);
		}
		if (this.jugadores.get(turno).getMano().size()!=1) {
			this.jugadores.get(turno).setProtegido_UNO(false);
		}
		if (esSalto && !hayReversa) {
			avanzarTurno();
		}
	}
	
	//FUNCIONA
	private boolean compruebaPuedeJugar() {
		Carta anterior = getUltimaCartaJugada();
		for(Carta c : jugadores.get(turno).getMano()) {
			if (c.esDelColor(anterior.getColor()) ||
			    c.esDelColor(Carta.Color.comodin) ||
			    Carta.compartenTipo(c, anterior)) {
				return true;
			}
		}
		return false;
	}
	
	//DESARROLLO
	/*public boolean compruebaPuedeJugar(int jugador) {
		Carta anterior = getUltimaCartaJugada();
		for(Carta c : jugadores.get(jugador).getMano()) {
			if (c.esDelColor(colorActual) ||
			    c.esDelColor(Carta.Color.comodin) ||
			    Carta.compartenTipo(c, anterior)) {
				return true;
			}
		}
		return false;
	}*/
	
	/**************************************************************************/
	// Funciones pÃºblicas
	/**************************************************************************/
	
	public void ejecutarJugada(Jugada jugada) {
		if(modoJugarCartaRobada) { //FUNCIONA
			if(jugada.getCartas()!=null && jugada.getCartas().size()==1) {
				juegaCarta(cartaRobada, jugada);
			}
			cartaRobada=null;
			modoJugarCartaRobada=false;
		} else if(jugada.isRobar()) {
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
			} else { //FUNCIONA
				this.cartaRobada = robarCarta(); //No se usaba la variable global, se usaba una local
				this.jugadores.get(turno).getMano().add(cartaRobada);
				if (cartaRobada.esDelColor(getUltimaCartaJugada().getColor()) || cartaRobada.esDelColor(Carta.Color.comodin) 
						|| Carta.compartenTipo(cartaRobada,getUltimaCartaJugada())) {
					modoJugarCartaRobada=true;
				}
			}
			
		} else {
			for (Carta c : jugada.getCartas()) {
				juegaCarta(c, jugada);
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
			Jugada jugadaIA = new Jugada();	// por defecto, robar
			
			if (compruebaPuedeJugar()) {
				Carta cartaCentral = getUltimaCartaJugada();
				
				if (modoAcumulandoRobo) {
					for (Carta c : this.jugadores.get(turno).getMano()) {
						if(compatibleAcumulador(c) && 
								(Carta.compartenTipo(c, cartaCentral)) 	//Si la carta es usable según las reglas
										|| c.esDelColor(getUltimaCartaJugada().getColor())  
										|| c.esDelTipo(Carta.Tipo.mas4)) {
							
							List<Carta> listaCartas = new ArrayList<>();
							listaCartas.add(c);
							jugadaIA.setCartas(listaCartas);
							jugadaIA.setRobar(false);
							break;
						}
					}
					
				} else if (modoJugarCartaRobada) {		
					List<Carta> listaCartas = new ArrayList<>();
					listaCartas.add(cartaRobada);
					jugadaIA.setCartas(listaCartas);
					jugadaIA.setRobar(false);
					
					if (cartaRobada.esDelColor(Carta.Color.comodin)) {
						int random_color = new Random().nextInt(4);
						switch(random_color) {
							case 0:
								cartaRobada.setColor(Carta.Color.amarillo);
								break;
							case 1:
								cartaRobada.setColor(Carta.Color.rojo);
								break;
							case 2:
								cartaRobada.setColor(Carta.Color.azul);
								break;
							case 3:
								cartaRobada.setColor(Carta.Color.verde);
								break;
						}
					}
					
				} else {
					for (Carta c : this.jugadores.get(turno).getMano()) {
						if (c.esCompatible(cartaCentral)) {
							List<Carta> listaCartas = new ArrayList<>();
							listaCartas.add(c);
							jugadaIA.setCartas(listaCartas);
							jugadaIA.setRobar(false);
							
							if (c.esDelColor(Carta.Color.comodin)) {
								int random_color = new Random().nextInt(4);
								switch(random_color) {
									case 0:
										c.setColor(Carta.Color.amarillo);
										break;
									case 1:
										c.setColor(Carta.Color.rojo);
										break;
									case 2:
										c.setColor(Carta.Color.azul);
										break;
									case 3:
										c.setColor(Carta.Color.verde);
										break;
								}
							}
							break;
						}
					}
				}	
				
				if (!validarJugada(jugadaIA)) {
					System.err.println("ERROR: la IA ha elegido una jugada no válida");
				}
			}
			
			System.err.println("Jugada elegida por la IA: " + jugadaIA);
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
		int numIAs = 0;
		for (Jugador j : this.jugadores) {
			if (j.isEsIA()) {
				numIAs++;
			}
		}
		return numIAs;
	}
	
	
	/**************************************************************************/
	// Para los FRONTENDs
	/**************************************************************************/
	
	// Se devuelven ordenados en sentido horario
	public List<Jugador> getJugadores() {
		return jugadores;
	}
	
	public int getTurno() {
		return turno;
	}
	
	// Devuelve -1 si no se ha encontrado
	public int getIndiceJugador(UUID jugadorID) {
		for (int i = 0; i < this.jugadores.size() ; i++) {
			if (jugadores.get(i).getJugadorID().equals(jugadorID)) {
				return i;
			}
		}
		return -1;
	}
	
	public Carta getUltimaCartaJugada() {
		return this.cartasJugadas.get(this.cartasJugadas.size()-1);
	}
	
	public boolean isSentidoHorario() {
		return sentidoHorario;
	}

	public void setSentidoHorario(boolean sentidoHorario) {
		this.sentidoHorario = sentidoHorario;
	}

	
	public boolean validarJugada(Jugada jugada) {
		if (jugada.isRobar()) { //FUNCINA
			if(jugadores.get(turno).getMano().size()>=20 && compruebaPuedeJugar()) { //No puede robar si puede jugar (sobraba el negado)
				return false;
			} else {
				return true;
			}
		} else if(modoJugarCartaRobada) {
			if(jugada.getCartas().isEmpty() || jugada.getCartas().get(0).equals(this.cartaRobada)) { //Si se juega la carta robada o ninguna
				return true;
			} else {
				return false;
			}
		}else if (jugada.getCartas() == null || jugada.getCartas().isEmpty()) {
			return false;
		} else if(modoAcumulandoRobo) {
			Carta anterior = getUltimaCartaJugada();
			if(jugada.getCartas().size()!=1) { //Solo se puede jugar una
				return false;
			} else {
				Carta c = jugada.getCartas().get(0);
				if(compatibleAcumulador(c) && (Carta.compartenTipo(c, anterior) //Si la carta es usable según las reglas
								|| c.esDelColor(getUltimaCartaJugada().getColor())  || c.esDelTipo(Carta.Tipo.mas4))) {
					return true;
				}
			}
	    } else { //FUNCIONA
			Carta anterior = getUltimaCartaJugada();
			boolean valida = false;
			Carta.Tipo tipo = jugada.getCartas().get(0).getTipo();
			
			//Las Ãºnicas cartas que hacen "jugadas" son los nÃºmeros, para el resto de cartas solo se puede jugar una.
			if(configuracion.getReglas().isJugarVariasCartas() && Carta.esNumero(tipo)) { //FUNCIONA
				int numCartas = 0; //Se necesitan dos para definir si son escaleras o iguales
				PosiblesTiposJugadas pj = new PosiblesTiposJugadas(false,false,false);
				for (Carta c : jugada.getCartas()) {
					if (numCartas<=1) {
						if(numCartas==0) {
							valida = Carta.compartenTipo(c, anterior) || c.esDelColor(getUltimaCartaJugada().getColor());
						} else {
							pj = evaluaJugada(anterior,c);
							valida = pj.valida;
						}
						numCartas++;
					} else {
						if(pj.esEscalera) {
							valida = Carta.sonConsecutivas(anterior,c);
						} else if(pj.esIguales){
							valida = Carta.compartenTipo(c, anterior);
						} else {
							valida = false;
						}
					}
					anterior = c;
					if(!valida) {
						break;
					}
				}
				//FUNCIONA
			} else { //Cartas con efecto o en general sin poder jugar varias cartas
				if (jugada.getCartas().size()>1) {
					valida = false; //Solo se puede jugar una si no son números. (o si no se permite jugar más de una).
				}else { //Decía true aun con más de una carta sin este else
					return Carta.compartenTipo(jugada.getCartas().get(0),anterior) 
							|| jugada.getCartas().get(0).esDelColor(getUltimaCartaJugada().getColor());
				}
				return Carta.compartenTipo(jugada.getCartas().get(0),anterior) 
						|| jugada.getCartas().get(0).esDelColor(getUltimaCartaJugada().getColor());
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


	@Override
	public String toString() {
		return "Partida [hayError=" + hayError + ", error=" + error + ", mazo=" + mazo + ", cartasJugadas="
				+ cartasJugadas + ", jugadores=" + jugadores + ", turno=" + turno + ", sentidoHorario=" + sentidoHorario
				+ ", configuracion=" + configuracion + ", terminada=" + terminada + ", fechaInicio=" + fechaInicio
				+ ", vistaPorRayosX=" + vistaPorRayosX + ", efectoRayosX=" + efectoRayosX + ", modoAcumulandoRobo="
				+ modoAcumulandoRobo + ", roboAcumulado=" + roboAcumulado + ", modoJugarCartaRobada="
				+ modoJugarCartaRobada + ", cartaRobada=" + cartaRobada + "]";
	}
	

	public Carta.Color getColorActual() {
		return getUltimaCartaJugada().getColor();
	}

	public boolean isModoAcumulandoRobo() {
		return modoAcumulandoRobo;
	}

	public void setModoAcumulandoRobo(boolean modoAcumulandoRobo) {
		this.modoAcumulandoRobo = modoAcumulandoRobo;
	}

	public boolean isModoJugarCartaRobada() {
		return modoJugarCartaRobada;
	}

	public void setModoJugarCartaRobada(boolean modoJugarCartaRobada) {
		this.modoJugarCartaRobada = modoJugarCartaRobada;
	}



	public Partida getPartidaAEnviar() {
		Partida partidaResumida = new Partida();
		
		partidaResumida.hayError = hayError;
		partidaResumida.error = error;
		
		partidaResumida.mazo = null;
		
		if (cartasJugadas != null && !cartasJugadas.isEmpty()) {
			partidaResumida.cartasJugadas = this.cartasJugadas.subList(this.cartasJugadas.size()-1, this.cartasJugadas.size());
		} else {
			partidaResumida.cartasJugadas = this.cartasJugadas;
		}
		
		
		partidaResumida.jugadores = jugadores;
		partidaResumida.turno = turno;
		partidaResumida.sentidoHorario = sentidoHorario;
		
		partidaResumida.configuracion = configuracion;
		partidaResumida.terminada = terminada;	
		
		//Fecha de inicio de la partida (Ya en formato sql porque no la necesita el frontend en este punto). 
		partidaResumida.fechaInicio = fechaInicio; 
		
		//Variables para extraer resultados de efectos
		partidaResumida.vistaPorRayosX = vistaPorRayosX;
		partidaResumida.efectoRayosX = efectoRayosX;
		partidaResumida.modoAcumulandoRobo = modoAcumulandoRobo;
		partidaResumida.roboAcumulado = roboAcumulado;
		partidaResumida.modoJugarCartaRobada = modoJugarCartaRobada;
		partidaResumida.cartaRobada = cartaRobada;
		
		return partidaResumida;
	}
	
	
}
