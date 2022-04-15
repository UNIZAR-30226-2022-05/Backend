package es.unizar.unoforall.gestores;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.db.PartidasDAO;
import es.unizar.unoforall.model.PartidasAcabadasVO;
import es.unizar.unoforall.model.partidas.HaJugadoVO;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ConfigSala.ModoJuego;
import es.unizar.unoforall.model.salas.Sala;

public class GestorSalas {
	private static HashMap<UUID,Sala> salas;
	
	private static final Object LOCK;
	
	static {
		salas = new HashMap<>();
		LOCK = new Object();
	}
	
	public static UUID nuevaSala(ConfigSala configuracion) {
		synchronized (LOCK) {
			UUID salaID = UUID.randomUUID();
			
			Sala sala = new Sala(configuracion);
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
				}
			}
		}
	}
	
	public static String insertarPartidaEnBd(Partida partida) {
		String error = null;
		PartidasAcabadasVO pa = new PartidasAcabadasVO(null, 
				partida.getFechaInicio(), 
				new Date(System.currentTimeMillis()), 
				partida.getNumIAs(),
				partida.getConfiguracion().getModoJuego().ordinal());
		
		ArrayList<HaJugadoVO> participantes = new ArrayList<HaJugadoVO>(); 
		
		ArrayList<Integer> puntos = new ArrayList<Integer>();
		for (Jugador j : partida.getJugadores()) {
			puntos.add(j.sacarPuntos()); //puntos.size()==configuracion.getMaxParticipantes()
		}
		int i = 0; //indice del jugador que estamos comprobando
		for (Jugador j : partida.getJugadores()) {
			if (!j.isEsIA()) {
				int usuariosDebajo = 0;
				boolean haGanado = false;
				if (puntos.get(i)==0) {
					haGanado = true;
					usuariosDebajo = partida.getConfiguracion().getMaxParticipantes()-1;
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
}
