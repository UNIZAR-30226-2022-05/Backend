package es.unizar.unoforall.salas;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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
				return salas;
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
						(configuracion.getReglas().equals(null)
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
}
