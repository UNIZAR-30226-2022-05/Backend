package es.unizar.unoforall.gestores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public static Sala eliminarParticipanteSalaExterno(UUID salaID, UUID usuarioID) {
		synchronized (LOCK) {
			return eliminarParticipanteSala(salaID, usuarioID);
		}
	}
	
	private static Sala eliminarParticipanteSala(UUID salaID, UUID usuarioID) {
		salas.get(salaID).eliminarParticipante(usuarioID);
		
		if(GestorSalas.salas.get(salaID).numParticipantes() == 0) {
			System.out.println("Eliminando sala " + salaID);
			salas.remove(salaID);
			return null;
		} else {
			GestorSesiones.getApiInterna().sendObject("/app/partidas/votacionesInternas/" + salaID, "vacio");
			
			return salas.get(salaID);
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
		synchronized (LOCK) {
			for(Map.Entry<UUID, Sala> entry : salas.entrySet()) {
				Sala sala = entry.getValue();
			    
				if (sala.isEnPausa() && sala.hayParticipante(usuarioID)) {
					return sala;
				}
			}
			return null;
		}
	}
	
	public static String insertarPartidaEnBd(UUID salaID) {
		synchronized (LOCK) {
			Partida partida = salas.get(salaID).getPartida();
			
			String error = null;
			PartidasAcabadasVO pa = new PartidasAcabadasVO(null, 
					partida.getFechaInicio(), 
					System.currentTimeMillis(), 
					partida.getNumIAs(),
					partida.getConfiguracion().getModoJuego().ordinal());
			
			ArrayList<HaJugadoVO> participantes = new ArrayList<HaJugadoVO>(); 
			boolean parejas = partida.getConfiguracion().getModoJuego().equals(ConfigSala.ModoJuego.Parejas);
				
			ArrayList<Integer> puntos = new ArrayList<Integer>();
			
			//Para tratar empates
			int numImplicados = 0;
			int empates = 0;
		
			for (Jugador j : partida.getJugadores()) { //Pueden haber empates hasta a tres bandas
				if (puntos.contains(j.sacarPuntos())) {
					numImplicados++; //Solo puede haber empate a un valor por partida (el ganador no puede empatar)
					empates = j.sacarPuntos();
				}
				puntos.add(j.sacarPuntos()); //puntos.size()==configuracion.getMaxParticipantes()
			}
			
			//Caso empates
			if(empates != 0) {
				boolean limSup = false;
				int limite = 0;
				ArrayList<Integer> auxiliar = new ArrayList<Integer>();
				auxiliar.add((int)(Math.random()*10000 + 1));
				auxiliar.add((int)(Math.random()*10000 + 1));
				if(numImplicados==3) { 						//No hay riesgo de generar empates
					auxiliar.add((int)(Math.random()*10000 + 1)); 		//Añadir tercera party
				} else {//Riesgo de generar empates
					int j = 0; //Para conocer la componente a actualizar en caso de limite superior.
					for(Integer p : puntos) {
						if (p!=empates && p!= 0) {
							if(p>empates) { //Si los valores a generar deben ser menores
								limSup=true;
								puntos.set(j, p+1); //para evitar caso 0, empate a 1, 2.
								limite=p+1;
							} else {
								limite=p;
							}
							break;
						}
						j++;
					}
				}
				ArrayList<Integer> orden = sacarOrden(auxiliar,limSup); //Si hay límite superior, genera valores negativos
				int indice = 0;
				for(int j = 0; j < puntos.size(); j++) {
					if (puntos.get(j)==empates) {
						puntos.set(j, limite + orden.get(indice));
						indice++;
					}
				}
			}
			
			//Caso modo por parejas
			if(parejas) { //Hace que las parejas tengan la misma puntuación
				for (int i = 0; i < 4; i++) {
					if (puntos.get(i)==0) { // Si es el ganador. i : 0 1 2 3
						puntos.set((i+1)%4, 1); //Rival				 1 2 3 0
						puntos.set((i+2)%4, 0); //Pareja			 2 3 0 1
						puntos.set((i+3)%4, 1); //Rival	 			 3 0 1 2
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
					/*//¿Posible modo puntos dobles?
					error = actualizarPuntosJugador(usuariosDebajo,j.getJugadorID());
					if (!error.equals("nulo")) {
						return error;
					}*/
					participantes.add(new HaJugadoVO(j.getJugadorID(),pa.getId(),usuariosDebajo,haGanado));				
				}
				i++;
			}
			//participantes.size()==configuracion.getMaxParticipantes()-numIAs
			
			ArrayList<Participante> listaParticipantes = new ArrayList<Participante>();
			for(HaJugadoVO part : participantes) {
				UsuarioVO usuario = UsuarioDAO.getUsuario(part.getUsuario());
				listaParticipantes.add(new Participante(usuario,part,partida.getJugadores().size(),
												partida.getConfiguracion().getModoJuego().ordinal()));
			}
			PartidaJugada pj = new PartidaJugada(pa,listaParticipantes);
			error = PartidasDAO.insertarPartidaAcabada(pj);
			
			
			// Para añadir IAs solo en la finalización de partidas (no en la BD)
			anyadirIAsParticipantes(pj, parejas, partida.getJugadores().size());
			
			salas.get(salaID).setUltimaPartidaJugada(pj);
			
			return error;
		}
	}
	
	/**
	 * Dados la partida jugada, si el modo es por parejas y el numero de jugadores total, añade a la lista de participantes las IAs
	 * que falten y adapta los puestos en caso de jugar en modo por parejas.
	 * 
	 * @param pj
	 * @param parejas
	 * @param numJugadores
	 */
	public static void anyadirIAsParticipantes(PartidaJugada pj, boolean parejas, int numJugadores) {
		List<Integer> listaPuestos = new ArrayList<>();
		int cuentaUno=0;
		int cuentaDos=0;
		for(Participante p : pj.getParticipantes()) {
			listaPuestos.add(p.getPuesto());
			if(p.getPuesto()==1) {
				cuentaUno++;
			} else if(p.getPuesto()==2) {
				cuentaDos++;
			}
		}
		
		if(!parejas) {
			for(Integer puesto = 1; puesto < numJugadores+1; puesto++) {
				if (!listaPuestos.contains(puesto)) {
					pj.agnadirParticipante(new Participante(puesto));
				}
			}
		} else {
			for (int j = cuentaUno; j < 2; j++) {
				pj.agnadirParticipante(new Participante(1));
			}
			for (int j = cuentaDos; j < 2; j++) {
				pj.agnadirParticipante(new Participante(2));
			}
			boolean primeraVez = true;
			for(int j = 0; j < 4; j++) {
				if(pj.getParticipantes().get(j).getPuesto()==2) {
					if(primeraVez) {
						pj.getParticipantes().get(j).setPuesto(3);
						primeraVez = false;
					} else {
						pj.getParticipantes().get(j).setPuesto(4);
						break;
					}
				}
			}
			for(int j = 0; j < 4; j++) {
				if(pj.getParticipantes().get(j).getPuesto()==1) {
					pj.getParticipantes().get(j).setPuesto(2);
					break;
				}
			}
		}
	}
	
//	private static String actualizarPuntosJugador(int usuariosDebajo, UUID jugadorID) {
//		synchronized (LOCK) {
//			String error = "nulo";
//			switch(usuariosDebajo) {
//				case 1://5
//					error = UsuarioDAO.actualizarPuntos(5, jugadorID);
//					break;
//				case 2://10
//					error = UsuarioDAO.actualizarPuntos(10, jugadorID);
//					break;
//				case 3://20
//					error = UsuarioDAO.actualizarPuntos(20, jugadorID);
//					break;
//			}
//			return error;
//		}
//	}
	
	private static ArrayList<Integer> sacarOrden(ArrayList<Integer> valores, boolean limSup) {
		ArrayList<Integer> orden = new ArrayList<Integer>();
		int i = valores.get(0);
		int j = valores.get(1);
		if (j > i) {
			if(!limSup) {
				orden.add(2);
				orden.add(1);
			} else {
				orden.add(-1);
				orden.add(-2);
			}
		} else { 
			if(!limSup) {
				orden.add(1);
				orden.add(2);
			} else {
				orden.add(-2);
				orden.add(-1);
			}
		}
		if(valores.size()==3) {//Empate a tres bandas
			int posicion = 3;
			if(valores.get(2) > i) {
				orden.set(0, orden.get(0)+1);
				posicion--;
			} 
			if(valores.get(2) > j) {
				orden.set(1, orden.get(1)+1);
				posicion--;
			} 
			orden.add(posicion);
		}
		return orden;
	}
	
	public static void restartTimer(UUID salaID) {
		synchronized (LOCK) {
			Timer timerTurno = timersSalas.get(salaID);		
			
			AlarmaFinTurno alarm = new AlarmaFinTurno(salaID);
			if(timerTurno != null) {
				timerTurno.cancel();
			}
			
			Sala sala = salas.get(salaID);
			if(sala != null && sala.isEnPartida()) {
				timerTurno = new Timer();
				timerTurno.schedule(alarm, Partida.TIMEOUT_TURNO);
				
				timersSalas.put(salaID, timerTurno);
			}
		}
	}
	
	public static void cancelTimer(UUID salaID) {
		synchronized (LOCK) {
			Timer timerTurno = timersSalas.get(salaID);
			
			if(timerTurno != null)
				timerTurno.cancel();
			
			timersSalas.remove(salaID);
		}
	}
	
}
