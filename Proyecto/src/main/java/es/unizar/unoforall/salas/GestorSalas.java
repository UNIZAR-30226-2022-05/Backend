package es.unizar.unoforall.salas;

import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;

public class GestorSalas {
	private static HashMap<UUID,Sala> salas;
	
	static {
		salas = new HashMap<>();
	}
	
	public static UUID nuevaSala(ConfigSala configuracion) {
		UUID salaID = UUID.randomUUID();
		
		Sala sala = new Sala(configuracion);
		salas.put(salaID, sala);
		return salaID;
	}
	
	public static Sala obtenerSala(UUID salaID) {
		return salas.get(salaID);
	}
	
	public static void eliminarSala(UUID salaID) {
		salas.remove(salaID);
	}
}
