package es.unizar.unoforall.sesiones;

import java.util.HashMap;
import javax.swing.Timer;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

public class GestorSesiones {
	
	// Relación  sesionID (websockets) - UsuarioID
	private static HashMap<String, UUID> sesiones;
	
	// Relación   clave inicio sesión - usuarioID
	private static HashMap<UUID, UUID> clavesInicio;
	
	static {
		sesiones = new HashMap<>();
		clavesInicio = new HashMap<>();
	}
	
	public static UUID nuevaClaveInicio(UUID usuarioID) {
		UUID claveInicio = UUID.randomUUID();
		clavesInicio.put(claveInicio, usuarioID);
		return claveInicio;
	}
	
	public static boolean iniciarSesion(UUID claveInicio, String sesionID) {
		if (clavesInicio.containsKey(claveInicio)) {
			sesiones.put(sesionID, clavesInicio.get(claveInicio));
			clavesInicio.remove(claveInicio);
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
