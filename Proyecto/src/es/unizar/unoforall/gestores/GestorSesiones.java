package es.unizar.unoforall.gestores;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import es.unizar.unoforall.api.WebSocketAPI;

public class GestorSesiones {
	
	// Relación  sesionID (websockets) - usuarioID
	private static HashMap<String, UUID> sesiones;
	
	// Relación   clave inicio sesión - usuarioID
	private static HashMap<UUID, UUID> clavesInicio;
	
	
	// Conexión propia de WebSockets para llamadas desde el servidor
	private static WebSocketAPI apiInterna;
	
	private static final Object LOCK;
	
	static {
		sesiones = new HashMap<>();
		clavesInicio = new HashMap<>();
		LOCK = new Object();
		apiInterna = new WebSocketAPI();
		try {
			apiInterna.openConnection();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public static String obtenerSesionID(UUID usuarioID) {
		synchronized (LOCK) {
			for (Entry<String, UUID> sesion : sesiones.entrySet()) {
		        if (sesion.getValue().equals(usuarioID)) {
		            return sesion.getKey();
		        }
		    }
			return null;
		}
	}
	
	public static void eliminarSesion(String sesionID) {
		synchronized (LOCK) {
			sesiones.remove(sesionID);
		}
	}

	public static WebSocketAPI getApiInterna() {
		return apiInterna;
	}

}
