package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.unizar.unoforall.model.salas.ConfigSala;

public class Partida {
	private List<Carta> mazo;
	private List<Carta> cartasJugadas;
	
	private List<Jugador> jugadores;
	private int turno;
	private boolean sentidoHorario;
	
	private ConfigSala configuracion;
	private boolean terminada;	
	
	private static final int MAX_ROBO_ATTACK = 10;
	
		
	public Partida(List<UUID> jugadoresID, int numIAs, ConfigSala configuracion) {
		//Mazo
		this.mazo = new LinkedList<>();
		for(Carta.Color color : Carta.Color.values()) {
			if (color != Carta.Color.comodin) {
				for(Carta.Tipo tipo : Carta.Tipo.values()) {
					if (tipo == Carta.Tipo.n0) {
						this.mazo.add(new Carta(tipo,color));
					} else {	//dos veces
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
		
		
		// Jugadores
		this.jugadores = new LinkedList<>();
		for(UUID jID : jugadoresID) {
			this.jugadores.add(new Jugador(jID));
		}
			// Se crean las IA
		for(int i = 0; i < numIAs; i++) {
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
	
	private Carta robarCarta() {
		
		Carta c = this.mazo.get(0);
		this.mazo.remove(0);
		return c;
	}
	

	/**************************************************************************/
	// Funciones públicas
	/**************************************************************************/
	
	public void ejecutarJugada(Jugada jugada, UUID jugadorID) {
		if (validarJugada(jugada) && 
				this.jugadores.get(turno).getJugadorID().equals(jugadorID)) {
			
			if(jugada.robar) {
				if (configuracion.getModoJuego() == ConfigSala.ModoJuego.Attack) {
					int random_robo = (int)Math.floor(Math.random()*(MAX_ROBO_ATTACK)+1);
					for (int i = 0; i < random_robo; i++) {
						this.jugadores.get(turno).getMano().add(robarCarta());
					}
				} else {
					this.jugadores.get(turno).getMano().add(robarCarta());
				}
			} else {
				for (Carta c : jugada.cartas) {
					switch (c.getTipo()) {
						case intercambio:
							List<Carta> nuevaMano = new ArrayList<>(siguienteJugador().getMano());
							siguienteJugador().getMano().clear();
							siguienteJugador().getMano().addAll(this.jugadores.get(turno).getMano());
							this.jugadores.get(turno).getMano().clear();
							this.jugadores.get(turno).getMano().addAll(nuevaMano);
							break;
							
						case mas2:
							for (int i = 0; i < 2; i++) {
								siguienteJugador().getMano().add(robarCarta());
							}
							break;
							
						case mas4:
							for (int i = 0; i < 4; i++) {
								siguienteJugador().getMano().add(robarCarta());
							}
							break;
							
						case x2:
							int numCartas = siguienteJugador().getMano().size();
							for (int i = 0; i < numCartas; i++) {
								siguienteJugador().getMano().add(robarCarta());
							}
							break;
							
						case rayosX:
							//llamada a websockets
							//TODO
							break;
							
						case reversa:
							this.sentidoHorario = ! this.sentidoHorario;
							break;
							
						case salta:
							avanzarTurno();
							break;
							
						default:
							break;
					}
					this.cartasJugadas.add(0, c);
				}
			}
			
			avanzarTurno();
			
			// Se comprueba si se ha acabado la partida
			for (Jugador j : this.jugadores) {
				if (j.getMano().size() == 0) {
					this.terminada = true;
				}
			}
		}
		
		//TODO
		//eventos asíncronos: la carta rayosX, emojis, botón de UNO, tiempo, votación pausa
	}
	
	public void ejecutarJugadaIA() {
		
	}
	
	public void expulsarJugador(UUID jugador) {
		//se sustituye por IA
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
		return this.cartasJugadas.get(0);
	}
	
	public boolean validarJugada(Jugada jugada) {
		if (jugada.robar) {
			return true;
		} else if (jugada.cartas == null) {
			return false;
		} else {
			Carta anterior = getUltimaCartaJugada();
			boolean valida = false;
			for (Carta c : jugada.cartas) {
				valida = c.esCompatible(anterior);
				anterior = c;
			}
			return valida;
		}
	}
	
	// Se debe mirar en cada turno, y cuando devuelva true ya se puede desconectar
	// del buzón de la partida con websockets
	public boolean estaTerminada() {
		return this.terminada;
	}
	
	public List<Jugador> ranking() {
		if (this.estaTerminada()) {
			List<Jugador> resultado = new ArrayList<>(this.jugadores);
			if(this.configuracion.getModoJuego().equals(ConfigSala.ModoJuego.Parejas)) {
				return null;
				//TODO
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
		} else {
			return null;
		}
	}
}
