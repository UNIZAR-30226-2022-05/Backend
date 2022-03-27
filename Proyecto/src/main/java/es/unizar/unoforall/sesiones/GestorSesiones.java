package es.unizar.unoforall.sesiones;

import java.util.HashMap;
import javax.swing.Timer;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

public class GestorSesiones {
	
	// Relación  sesionID (websockets) - UsuarioID
	private static HashMap<String, UUID> sesiones;
	
	// Relación  usuarioID - clave inicio sesión
	private static HashMap<UUID, UUID> clavesInicio;
	
	static {
		sesiones = new HashMap<>();
		clavesInicio = new HashMap<>();
	}
	
	public static UUID nuevaClaveInicio(UUID usuarioID) {
		UUID claveInicio = UUID.randomUUID();
		clavesInicio.put(usuarioID, claveInicio);
		return claveInicio;
	}
	
	public static boolean iniciarSesion(UUID usuarioID, UUID claveInicio, String sesionID) {
		if (clavesInicio.get(usuarioID).equals(claveInicio)) {
			sesiones.put(sesionID, usuarioID);
			return true;
		} else {
			return false;
		}
	}
	
	public static UUID obtenerUsuarioID(String sesionID) {
		return sesiones.get(sesionID);
	}
	
	public static void eliminarSesion(String sesionID) {
		sesiones.remove(sesionID);
	}
}
