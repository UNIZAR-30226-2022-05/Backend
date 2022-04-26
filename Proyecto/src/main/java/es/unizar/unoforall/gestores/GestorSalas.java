package es.unizar.unoforall.gestores;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import es.unizar.unoforall.db.PartidasDAO;
import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.model.PartidasAcabadasVO;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.HaJugadoVO;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.partidas.Participante;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ConfigSala.ModoJuego;
import es.unizar.unoforall.model.salas.Sala;

public class GestorSalas {
	private static HashMap<UUID,Sala> salas;
	private static HashMap<UUID,Timer> timersSalas;
	
	private static final Object LOCK;
	
	static {
		salas = new HashMap<>();
		timersSalas = new HashMap<>();
		LOCK = new Object();
	}
	
	public static UUID nuevaSala(ConfigSala configuracion) {
		synchronized (LOCK) {
			UUID salaID = UUID.randomUUID();
			
			Sala sala = new Sala(configuracion, salaID);
			salas.put(salaID, sala);
			return salaID;
		}
	}
	
	public static Sala obtenerSala(UUID salaID) {
		synchronized (LOCK) {
			return salas.get(salaID);
		}
	}
	
	public static Sala buscarSalaID(UUID salaID) {
		synchronized (LOCK) {
			Sala sala = salas.get(salaID);
			if (sala.puedeUnirse()) {
				return sala;
			} else {
				return null;
			}
		}
	}
	
	public static HashMap<UUID,Sala> buscarSalas(ConfigSala configuracion) {
		synchronized (LOCK) {
			if (configuracion == null) {
				return new HashMap<UUID,Sala>();
			} else {
				HashMap<UUID,Sala> result = new HashMap<>();
				for(Map.Entry<UUID, Sala> entry : salas.entrySet()) {
					UUID salaID = entry.getKey();
					Sala sala = entry.getValue();
				    
					if (sala.puedeUnirse()
							&&
						(configuracion.getModoJuego().equals(ModoJuego.Undefined)
						|| configuracion.getModoJuego().equals(sala.getConfiguracion().getModoJuego()))
							&&
						(configuracion.getMaxParticipantes() == -1
						|| configuracion.getMaxParticipantes() == sala.getConfiguracion().getMaxParticipantes())	
							&&
						(!configuracion.getReglas().isReglasValidas()
						|| configuracion.getReglas().equals(sala.getConfiguracion().getReglas()))
						){
						result.put(salaID, sala);
					} 
				}
				return result;
			}
		}
	}
	
	public static void eliminarSala(UUID salaID) {
		synchronized (LOCK) {
			salas.remove(salaID);
		}
	}
	
	public static Sala eliminarParticipanteSala(UUID salaID, UUID usuarioID) {
		synchronized (LOCK) {
			GestorSalas.obtenerSala(salaID).eliminarParticipante(usuarioID);
			
			if(GestorSalas.obtenerSala(salaID).numParticipantes() == 0) {
				System.out.println("Eliminando sala " + salaID);
				GestorSalas.eliminarSala(salaID);
				return null;
			} else {
				return GestorSalas.obtenerSala(salaID);
			}
		}
	}
	
	public static void eliminarParticipanteSalas(UUID usuarioID) {
		synchronized (LOCK) {
			for(Map.Entry<UUID, Sala> entry : new HashMap<>(salas).entrySet()) {
				UUID salaID = entry.getKey();
				Sala sala = entry.getValue();
				
				if (sala.hayParticipante(usuarioID)) {
					System.out.println("Eliminando participante desconectado");
					eliminarParticipanteSala(salaID, usuarioID);
					
					GestorSesiones.getApiInterna().sendObject("/app/salas/actualizar/" + salaID, "vacio");
				}
			}
		}
	}
	
	public static Sala getSalaPausada(UUID usuarioID) {
		for(Map.Entry<UUID, Sala> entry : salas.entrySet()) {
			Sala sala = entry.getValue();
		    
			if (sala.getParticipantesVotoAbandono().containsKey(usuarioID)) {
				return sala;
			}
		}
		return null;
	}
	
	public static String insertarPartidaEnBd(UUID salaID) {
		synchronized (LOCK) {
			Partida partida = obtenerSala(salaID).getPartida();
			
			String error = null;
			PartidasAcabadasVO pa = new PartidasAcabadasVO(null, 
					partida.getFechaInicio(), 
					new Date(System.currentTimeMillis()), 
					partida.getNumIAs(),
					partida.getConfiguracion().getModoJuego().ordinal());
			
			ArrayList<HaJugadoVO> participantes = new ArrayList<HaJugadoVO>(); 
			boolean parejas = partida.getConfiguracion().getModoJuego().equals(ConfigSala.ModoJuego.Parejas);
			
			//if(!parejas) { //Versión para calcular puntos por parejas descartada por ser demasiado farragosa.
				
			ArrayList<Integer> puntos = new ArrayList<Integer>();
			for (Jugador j : partida.getJugadores()) {
				puntos.add(j.sacarPuntos()); //puntos.size()==configuracion.getMaxParticipantes()
			}
			if(parejas) { //Hace que las parejas tengan la misma puntuación
				for (int i = 0; i < 4; i++) {
					if(puntos.get(i)==0) {
						puntos = new ArrayList<Integer>();
						if (i==0 || i==2) {
							puntos.add(0);
							puntos.add(1);
							puntos.add(0);
							puntos.add(1);
						} else {
							puntos.add(1);
							puntos.add(0);
							puntos.add(1);
							puntos.add(0);
						}
						break;
					}
				}
			}
			int i = 0; //indice del jugador que estamos comprobando
			for (Jugador j : partida.getJugadores()) {
				if (!j.isEsIA()) {
					int usuariosDebajo = 0;
					boolean haGanado = false;
					if (puntos.get(i)==0) {
						haGanado = true;
						if (parejas) {
							usuariosDebajo = 2;
						} else {
							usuariosDebajo = partida.getConfiguracion().getMaxParticipantes()-1;
						}
					} else {
						for(Integer p : puntos) {
							if(p>puntos.get(i)) { //En caso de usuarios empatados ninguno está por debajo de otro.
								usuariosDebajo++; //No es necesario preocuparse por compararse consigo mismo porque
							}					  //cuenta como empate.
						}
					}
					error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
					if (!error.equals("nulo")) {
						return error;
					}
					participantes.add(new HaJugadoVO(j.getJugadorID(),pa.getId(),usuariosDebajo,haGanado));				
				}
				i++;
			}
			/*} else { //Versión para calcular puntos por parejas descartada por ser demasiado farragosa.
				int i = 0; //indice del jugador que estamos comprobando
				for (Jugador j : partida.getJugadores()) {
					if (j.getMano().size()==0) { //Si es el ganador realizar la operación
						boolean haGanado = true;
						int usuariosDebajo = 2; // La pareja perdedora
						participantes.add(new HaJugadoVO(j.getJugadorID(),pa.getId(),usuariosDebajo,haGanado));
						switch(i) {
							case 0: //Pareja 2
								if (!partida.getJugadores().get(1).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(2).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																										usuariosDebajo,haGanado));
									error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
									if (!error.equals("nulo")) {
										return error;
									}
								}
								if (!partida.getJugadores().get(3).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
							case 1: //Pareja 3
								if (!partida.getJugadores().get(0).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(2).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(3).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																										usuariosDebajo,haGanado));
									error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
									if (!error.equals("nulo")) {
										return error;
									}
								}
							case 2: //Pareja 0
								if (!partida.getJugadores().get(0).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																										usuariosDebajo,haGanado));
									error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
									if (!error.equals("nulo")) {
										return error;
									}
								}
								if (!partida.getJugadores().get(1).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(3).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
							case 3: //Pareja 1
								if (!partida.getJugadores().get(0).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(2).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																														0,false));
								}
								if (!partida.getJugadores().get(3).isEsIA()) {
									participantes.add(new HaJugadoVO(partida.getJugadores().get(2).getJugadorID(),pa.getId(),
																										usuariosDebajo,haGanado));
									error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
									if (!error.equals("nulo")) {
										return error;
									}
								}
						}
						if (!j.isEsIA()) {
							error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
							if (!error.equals("nulo")) {
								return error;
							}
						}
						break; //Ya se han añadido los participantes y se han actualizado los puntos.
					}	
					i++;
				}
			}*/
			//participantes.size()==configuracion.getMaxParticipantes()-numIAs
			ArrayList<Participante> listaParticipantes = new ArrayList<Participante>();
			for(HaJugadoVO part : participantes) {
				UsuarioVO usuario = UsuarioDAO.getUsuario(part.getUsuario());
				listaParticipantes.add(new Participante(usuario,part));
			}
			PartidaJugada pj = new PartidaJugada(pa,listaParticipantes);
			error = PartidasDAO.insertarPartidaAcabada(pj);
			obtenerSala(salaID).setUltimaPartidaJugada(pj);
			
			return error;
		}
	}
	
	private static String actualizarPuntosJugador(int usuariosDebajo, UUID jugadorID) {
		synchronized (LOCK) {
			String error = "nulo";
			switch(usuariosDebajo) {
				case 1://5
					error = UsuarioDAO.actualizarPuntos(5, jugadorID);
					break;
				case 2://10
					error = UsuarioDAO.actualizarPuntos(10, jugadorID);
					break;
				case 3://20
					error = UsuarioDAO.actualizarPuntos(20, jugadorID);
					break;
			}
			return error;
		}
	}
	
	public static void restartTimer(UUID salaID) {
		Timer timerTurno = timersSalas.get(salaID);		
		
		AlarmaFinTurno alarm = new AlarmaFinTurno(salaID);
		if(timerTurno != null)
			timerTurno.cancel();
		
		if(obtenerSala(salaID).isEnPartida()) {
			timerTurno = new Timer();
			timerTurno.schedule(alarm, Partida.TIMEOUT_TURNO);
			
			timersSalas.put(salaID, timerTurno);
		}
	}
	
	public static void cancelTimer(UUID salaID) {
		Timer timerTurno = timersSalas.get(salaID);
		
		if(timerTurno != null)
			timerTurno.cancel();
		
		timersSalas.remove(salaID);
	}
}
