package es.unizar.unoforall.sesiones;

import java.util.HashMap;
import javax.swing.Timer;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

public class GestorSesiones {
	
	// Relación  sesionID (websockets) - usuarioID
	private static HashMap<String, UUID> sesiones;
	
	// Relación   clave inicio sesión - usuarioID
	private static HashMap<UUID, UUID> clavesInicio;
	
	private static final Object LOCK;
	
	static {
		sesiones = new HashMap<>();
		clavesInicio = new HashMap<>();
		LOCK = new Object();
	}
	
	public static UUID nuevaClaveInicio(UUID usuarioID) {
		synchronized (LOCK) {
			if (sesiones.containsValue(usuarioID) || clavesInicio.containsValue(usuarioID))  {
				return null;
			} else {
				UUID claveInicio = UUID.randomUUID();
				clavesInicio.put(claveInicio, usuarioID);
				return claveInicio;
			}
		}
	}
	
	public static boolean iniciarSesion(UUID claveInicio, String sesionID) {
		synchronized (LOCK) {
			if (clavesInicio.containsKey(claveInicio)) {
				sesiones.put(sesionID, clavesInicio.get(claveInicio));
				clavesInicio.remove(claveInicio);
				return true;
			} else {
				return false;
			}
		}
	}
	
	public static UUID obtenerUsuarioID(String sesionID) {
		synchronized (LOCK) {
			return sesiones.get(sesionID);
		}
	}
	
	public static void eliminarSesion(String sesionID) {
		synchronized (LOCK) {
			sesiones.remove(sesionID);
		}
	}
}
