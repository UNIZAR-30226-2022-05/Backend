package es.unizar.unoforall.apirest.partida;

import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partida.ConfigSala;

public class GestorSalas {
	private static HashMap<UUID,Sala> salas;
	
	static {
		salas = new HashMap<>();
	}
	
	public static UUID nuevaSala(ConfigSala configuracion, UsuarioVO creador) {
		UUID salaID = UUID.randomUUID();
		
		Sala sala = new Sala(configuracion, creador);
		salas.put(salaID, sala);
		return salaID;
	}
	
	public static Sala obtenerSala(UUID salaID) {
		return salas.get(salaID);
	}
	
	public static void eliminarSesion(UUID salaID) {
		salas.remove(salaID);
	}
}
