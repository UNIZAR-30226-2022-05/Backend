package es.unizar.unoforall.gestores;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.i2000c.web_utils.client.WebsocketClient;

public class GestorSesiones {
	
	// Relación  sesionID (websockets) - usuarioID
	private static Map<UUID, UUID> sesiones;
	
	// Relación   clave inicio sesión - usuarioID
	//private static HashMap<UUID, UUID> clavesInicio;
	
	
	// Conexión propia de WebSockets para llamadas desde el servidor
	private static WebsocketClient apiInterna;
	
	//private static final Object LOCK;
	
	public static void inicializar() {
		sesiones = new ConcurrentHashMap<>();
		//clavesInicio = new HashMap<>();
		//LOCK = new Object();
                
		apiInterna = new WebsocketClient("http://localhost");
                apiInterna.openConnection("/topic");
                System.out.println("API interna iniciada con id de sesión: " + apiInterna.getSessionID());
	}
	
	/*public static UUID nuevaClaveInicio(UUID usuarioID) {
		synchronized (LOCK) {
			if (sesiones.containsValue(usuarioID) || clavesInicio.containsValue(usuarioID))  {
				return null;
			} else {
				UUID claveInicio = UUID.randomUUID();
				clavesInicio.put(claveInicio, usuarioID);
				return claveInicio;
			}
		}
	}*/
        
        public static boolean estaLogueado(UUID usuarioID){
            return sesiones.containsValue(usuarioID);
        }
        
        public static void loguearUsuario(UUID sessionID, UUID usuarioID){
            sesiones.put(sessionID, usuarioID);
        }
	
	/*public static boolean iniciarSesion(UUID claveInicio, UUID sesionID) {
		synchronized (LOCK) {
			if (clavesInicio.containsKey(claveInicio)) {
				sesiones.put(sesionID, clavesInicio.get(claveInicio));
				clavesInicio.remove(claveInicio);
				return true;
			} else {
				return false;
			}
		}
	}*/
	
	public static UUID obtenerUsuarioID(UUID sesionID) {
		return sesiones.get(sesionID);
	}
	
	public static UUID obtenerSesionID(UUID usuarioID) {
		for (Entry<UUID, UUID> sesion : sesiones.entrySet()) {
		        if (sesion.getValue().equals(usuarioID)) {
		            return sesion.getKey();
		        }
		    }
		return null;
	}
	
	public static void eliminarSesion(UUID sesionID) {
		sesiones.remove(sesionID);
	}

	public static WebsocketClient getApiInterna() {
		return apiInterna;
	}

}
