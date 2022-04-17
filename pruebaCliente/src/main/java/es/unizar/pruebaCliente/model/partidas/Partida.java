package es.unizar.pruebaCliente.model.partidas;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.unizar.pruebaCliente.model.salas.ConfigSala;


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
	private Date fechaInicio; //Fecha de inicio de la partida (Ya en formato sql porque no la necesita el frontend en este punto). 
	private Carta.Color colorActual;
	private boolean esCambioDeColor;
	
	//Variables para extraer resultados de efectos
	private Carta vistaPorRayosX;
	private boolean efectoRayosX;
	private boolean modoAcumulandoRobo;
	private int roboAcumulado;
	
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
		
		//Marcamos fecha de inicio
		fechaInicio = new Date(System.currentTimeMillis()); //Fecha actual.
		
		//Mazo
		this.mazo = new LinkedList<>();
		for(Carta.Color color : Carta.Color.values()) {
			if (color != Carta.Color.comodin) {
				for(Carta.Tipo tipo : Carta.Tipo.values()) {
					if (tipo.equals(Carta.Tipo.n0)) {
						this.mazo.add(new Carta(tipo,color));
					} else if (compruebaIncluir(tipo)) {	//dos veces si tienen color y están aceptadas por la configuración
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
		this.cartasJugadas = new ArrayList<>();
		this.cartasJugadas.add(getCartaValida());
		
		
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
		
		
		// Resto
		this.turno = 0;
		this.sentidoHorario = true;
		this.configuracion = configuracion;
		this.terminada = false;
	}


	/**************************************************************************/
	// Funciones privadas
	/**************************************************************************/
	
	private boolean compatibleAcumulador(Carta c) {
		if (configuracion.getReglas().isEncadenarRoboCartas() && (c.getTipo().equals(Carta.Tipo.mas4) || c.getTipo().equals(Carta.Tipo.mas2)) 
				|| configuracion.getReglas().isRedirigirRoboCartas() && c.getTipo().equals(Carta.Tipo.reversa) ) {
			return true;
		} else {
			return false;
		}
	}
	
	private Carta getCartaValida() {
		Carta carta = this.mazo.get(0);
		while(carta.getTipo()==Carta.Tipo.cambioColor 
				|| carta.getTipo()==Carta.Tipo.mas2 
				|| carta.getTipo()==Carta.Tipo.mas4 
				|| carta.getTipo()==Carta.Tipo.x2 
				|| carta.getTipo()==Carta.Tipo.rayosX 
				|| carta.getTipo()==Carta.Tipo.salta 
				|| carta.getTipo()==Carta.Tipo.intercambio 
				|| carta.getTipo()==Carta.Tipo.reversa) {
			Collections.shuffle(this.mazo);
			carta = this.mazo.get(0);
		}
		this.mazo.remove(0);
		colorActual = carta.getColor();
		return carta;
	}
	
	private boolean compruebaIncluir(Carta.Tipo tipo) {
		if (tipo == Carta.Tipo.rayosX && configuracion.getReglas().isCartaRayosX()) {
			return true;
		} else if (tipo == Carta.Tipo.intercambio && configuracion.getReglas().isCartaIntercambio()) {
			return true;
		} else if (tipo == Carta.Tipo.x2 && configuracion.getReglas().isCartaX2()){
			return true;
		} else if (tipo != Carta.Tipo.cambioColor &&  tipo != Carta.Tipo.mas4) {
			return true;
		}
		return false;
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
	
	private PosiblesTiposJugadas evaluaJugada(Carta c1, Carta c2) {
		PosiblesTiposJugadas pj = null;
		if (c1.getTipo().equals(c2.getTipo())) {
			pj = new PosiblesTiposJugadas(false,true,true);
		} else if(c1.getTipo().ordinal()==c2.getTipo().ordinal()-1 
				|| c1.getTipo()==Carta.Tipo.n9 && c2.getTipo()==Carta.Tipo.n0) {
			pj = new PosiblesTiposJugadas(true,false,true);
		} else {
			pj = new PosiblesTiposJugadas(false,false,false);
		}
		return pj;
	}
	
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
	/*
	
	*/

	/**************************************************************************/
	// Funciones públicas
	/**************************************************************************/
	
	public void ejecutarJugada(Jugada jugada) {
		if(jugada.robar) {
				if(modoAcumulandoRobo) {
					modoAcumulandoRobo=false;
					for(int i = 0; i<roboAcumulado; i++) {
						if(this.jugadores.get(turno).getMano().size()==20) {
							break;
						}
						this.jugadores.get(turno).getMano().add(robarCarta());
					}
					roboAcumulado=0;
				} else if (configuracion.getModoJuego().equals(ConfigSala.ModoJuego.Attack)) {
				int random_robo = (int)Math.floor(Math.random()*(MAX_ROBO_ATTACK)+1);
				for (int i = 0; i < random_robo; i++) {
					if(this.jugadores.get(turno).getMano().size()==20) {
						break;
					}
					this.jugadores.get(turno).getMano().add(robarCarta());
				}
			} else {
				if(this.jugadores.get(turno).getMano().size()<20) {
					this.jugadores.get(turno).getMano().add(robarCarta());
				}
				
			}
		} else {
			for (Carta c : jugada.cartas) {
				esCambioDeColor = false;
				efectoRayosX = false;
				boolean esSalto = false;
				switch (c.getTipo()) {
					case intercambio:
						List<Carta> nuevaMano = new ArrayList<>(siguienteJugador().getMano());
						siguienteJugador().getMano().clear();
						siguienteJugador().getMano().addAll(this.jugadores.get(turno).getMano());
						this.jugadores.get(turno).getMano().clear();
						this.jugadores.get(turno).getMano().addAll(nuevaMano);
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
								if(siguienteJugador().getMano().size()==20) {
									break;
								}
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
								if(siguienteJugador().getMano().size()==20) {
									break;
								}
								siguienteJugador().getMano().add(robarCarta());
							}
							esSalto=true;
						}
						esCambioDeColor = true;
						colorActual = jugada.nuevoColor;
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
						List<Carta> mano = siguienteJugador().getMano();
						Collections.shuffle(mano);
						vistaPorRayosX = mano.get(0);
						efectoRayosX = true;
						break;
						
					case reversa:
						this.sentidoHorario = ! this.sentidoHorario;
						break;
						
					case salta:
						esSalto = true;//avanzarTurno();
						break;
						
					case cambioColor:
						esCambioDeColor = true;
						colorActual = jugada.nuevoColor;
						break;
						
					default:
						break;
				}
				this.cartasJugadas.add(c); //La añade al final (por implementaciones de rellenar y robar del mazo);
				this.jugadores.get(turno).getMano().remove(c);
				if (this.jugadores.get(turno).getMano().size()!=1) {
					this.jugadores.get(turno).setProtegido_UNO(false);
				}
				if (esSalto) {
					avanzarTurno();
				}
			}
			if (!esCambioDeColor) {
				colorActual = getUltimaCartaJugada().getColor();
			}
			
		}
		
		avanzarTurno();
		
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
			return true;
		} else if (jugada.cartas == null || jugada.cartas.isEmpty()) {
			return false;
		} else if(modoAcumulandoRobo) {
			Carta anterior = getUltimaCartaJugada();
			if(jugada.getCartas().size()!=1) { //Solo se puede jugar una
				return false;
			} else {
				Carta c = jugada.getCartas().get(0);
				if(compatibleAcumulador(c) && (c.getTipo().equals(anterior.getTipo()) //Si la carta es usable según las reglas
								|| c.getColor().equals(colorActual)  || c.getTipo().equals(Carta.Tipo.mas4))) {
					return true;
				}
			}
	    } else {
			Carta anterior = getUltimaCartaJugada();
			boolean valida = false;
			Carta.Tipo tipo = jugada.getCartas().get(0).getTipo();
			//Las únicas cartas que hacen "jugadas" son los números, para el resto de cartas solo se puede jugar una.
			if(configuracion.getReglas().isJugarVariasCartas() && (tipo == Carta.Tipo.n0 || tipo == Carta.Tipo.n1 
					|| tipo == Carta.Tipo.n2 || tipo == Carta.Tipo.n3 || tipo == Carta.Tipo.n4 || tipo == Carta.Tipo.n5 
					|| tipo == Carta.Tipo.n6 || tipo == Carta.Tipo.n7 || tipo == Carta.Tipo.n9)) {
				int numCartas = 0; //Se necesitan dos para definir si son escaleras o iguales
				PosiblesTiposJugadas pj = new PosiblesTiposJugadas(false,false,false);
				for (Carta c : jugada.cartas) {
					if (numCartas<=1) {
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
									anterior.getTipo()==Carta.Tipo.n9 && c.getTipo()==Carta.Tipo.n0;
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
			//TODO verificar si se hace bien la escalera... (igual mejor en los frontend)
		}
		return false;
	}
	
	// Se debe mirar en cada turno, y cuando devuelva true ya se puede desconectar
	// del buzón de la partida con websockets
	public boolean estaTerminada() {
		return this.terminada;
	}
	
	//Considero que esto es eliminable
	/*
	public List<Jugador> ranking() {
		if (this.estaTerminada()) {
			List<Jugador> resultado = new ArrayList<>(this.jugadores);
			if(this.configuracion.getModoJuego().equals(ConfigSala.ModoJuego.Parejas)) {
				return null;
				//TODO Más adelante (primero solo UNO original)
			} else {
				Collections.sort(resultado, new Comparator<Jugador>() {
				  @Override
				  public int compare(Jugador j1, Jugador j2) {
					  if (j1.getMano().size() == j2.getMano().size()) {
						  return 0;
					  } else if (j1.getMano().size() < j2.getMano().size()) {
						  return -1;
					  } else {
						  return 1;  
					  }
				  }
				});
				return resultado;
			}
			//TODO asignar los puntos a cada jugador
		} else {
			return null;
		}
	}*/


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
		//Caso sentido horario 0->max y anti-horario max->0
		if(efectoRayosX && jugadorID.equals(anteriorJugador().getJugadorID())) {
			return vistaPorRayosX; //Si el jugador ha jugado una rayosX en el turno anterior
		}
		return null; //Si el jugador no ha jugado una rayosX en el turno anterior.
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
