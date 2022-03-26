package es.unizar.unoforall.apirest;

import java.util.HashMap;
import javax.swing.Timer;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

public class GestorSesiones {
	
	private static HashMap<UUID,Sesion> sesiones;
	private final static int TIEMPO_EXPIRACION_SESION = 5*60000;  //5 minutos
	
	static {
		sesiones = new HashMap<>();
	}
	
	
	public static UUID nuevaSesion(UsuarioVO usuario) {
		UUID sesionID = UUID.randomUUID();
		
		Timer t = new Timer(TIEMPO_EXPIRACION_SESION, new AlarmaSesion(sesionID));
		Sesion sesion = new Sesion(usuario, t);
		
		sesiones.put(sesionID, sesion);
		t.start();
		return sesionID;
	}
	
	public static Sesion obtenerSesion(UUID sessionID) {
		return sesiones.get(sessionID);
	}
	
	public static void eliminarSesion(UUID sessionID) {
		GestorSesiones.obtenerSesion(sessionID).getTimer().stop(); //Para evitar que salte una alarma inexistente
		sesiones.remove(sessionID);
	}
	
}
