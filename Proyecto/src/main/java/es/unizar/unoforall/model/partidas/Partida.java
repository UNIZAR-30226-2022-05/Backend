package es.unizar.unoforall.model.partidas;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.unizar.unoforall.db.PartidasDAO;
import es.unizar.unoforall.model.PartidasAcabadasVO;
import es.unizar.unoforall.model.salas.ConfigSala;

public class Partida {
	private boolean hayError;
	private String error;
	
	private List<Carta> mazo;
	private List<Carta> cartasJugadas;
	
	private List<Jugador> jugadores;
	private int turno;
	private boolean sentidoHorario;
	private int numIAs;
	
	private ConfigSala configuracion;
	private boolean terminada;	
	private Date fechaInicio; //Fecha de inicio de la partida (Ya en formato sql porque no la necesita el frontend en este punto). 
	
	private static final int MAX_ROBO_ATTACK = 10;
	
	public Partida(String error) {			//Para construir una partida con error = true
		this.setHayEror(true);
		this.setError(error);
	}
		
	public Partida(List<UUID> jugadoresID, ConfigSala configuracion) {
		this.setHayEror(false);
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
					} else if (tipo != Carta.Tipo.cambioColor &&  tipo != Carta.Tipo.mas4) {	//dos veces
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
			//TODO meter solo las cartas especiales que estén en configuración
		}
		Collections.shuffle(this.mazo);
		
		
		// Cartas jugadas
		this.cartasJugadas = new ArrayList<>();
		// TODO poner primera carta
		
		
		// Jugadores
		this.jugadores = new LinkedList<>();
		for(UUID jID : jugadoresID) {
			this.jugadores.add(new Jugador(jID));
		}
			// Se crean las IA
		numIAs = configuracion.getMaxParticipantes() - this.jugadores.size();
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
		//TODO cuando no haya para robar, coger todas las jugadas menos la última, shuffle, y meterlas en el mazo
		Carta c = this.mazo.get(0);
		this.mazo.remove(0);
		return c;
	}
	
	private String insertarPartidaEnBd() {
		String error = null;
		PartidasAcabadasVO pa = new PartidasAcabadasVO(null,fechaInicio,new Date(System.currentTimeMillis()),numIAs,configuracion.getModoJuego().ordinal());
		ArrayList<HaJugadoVO> participantes = new ArrayList<HaJugadoVO>(); 
		
		ArrayList<Integer> puntos = new ArrayList<Integer>();
		for (Jugador j : this.jugadores) {
			puntos.add(j.sacarPuntos()); //puntos.size()==configuracion.getMaxParticipantes()
		}
		int i = 0; //indice del jugador que estamos comprobando
		for (Jugador j : this.jugadores) {
			if (!j.isEsIA()) {
				int usuariosDebajo = 0;
				boolean haGanado = false;
				if (puntos.get(i)==0) {
					haGanado = true;
					usuariosDebajo = configuracion.getMaxParticipantes()-1;
				} else {
					for(Integer p : puntos) {
						if(p>puntos.get(i)) { //En caso de usuarios empatados ninguno está por debajo de otro.
							usuariosDebajo++; //No es necesario preocuparse por compararse consigo mismo porque
						}					  //cuenta como empate.
					}
				}
				participantes.add(new HaJugadoVO(j.getJugadorID(),pa.getId(),usuariosDebajo,haGanado));				
			}
			i++;
		}
		//participantes.size()==configuracion.getMaxParticipantes()-numIAs
		PartidaJugada pj = new PartidaJugada(pa,participantes);
		error = PartidasDAO.insertarPartidaAcabada(pj);
		return error;
	}
	

	/**************************************************************************/
	// Funciones públicas
	/**************************************************************************/
	
	public void ejecutarJugada(Jugada jugada) {
		if(jugada.robar) {
			if (configuracion.getModoJuego().equals(ConfigSala.ModoJuego.Attack)) {
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
				//TODO eliminar la carta de la mano del jugador; hacer antes de las acciones
			}
		}
		
		avanzarTurno();
		
		// Se comprueba si se ha acabado la partida
		for (Jugador j : this.jugadores) {
			if (j.getMano().size() == 0) {
				this.terminada = true;
				//TODO meter partida en la BD
			}
		}
		if (this.terminada) {
			String error = insertarPartidaEnBd();
			if (!error.equals("nulo")) {
				//TODO Tratamiento de error al insertar en base de datos
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
			//TODO crear jugada según modo de juego y cartas jugables
			
			ejecutarJugada(jugadaIA);
		}
	}
	
	public boolean turnoDeIA() {
		return this.jugadores.get(turno).isEsIA();
	}
	
	public void expulsarJugador(UUID jugador) {
		//se sustituye por IA
		//TODO
	}
	
	public void pulsarBotonUNO(UUID jugador) {
		//TODO
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
			//TODO verificar si se hace bien la escalera... (igual mejor en los frontend)
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
			//TODO asignar los puntos a cada jugador
		} else {
			return null;
		}
	}


	public boolean isHayError() {
		return hayError;
	}


	public void setHayEror(boolean hayError) {
		this.hayError = hayError;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
