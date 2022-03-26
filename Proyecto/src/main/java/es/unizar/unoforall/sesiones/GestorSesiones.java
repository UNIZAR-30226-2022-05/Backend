package es.unizar.unoforall.sesiones;

import java.util.HashMap;
import javax.swing.Timer;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

public class GestorSesiones {
	
	private static HashMap<UUID,Sesion> sesiones;
	private final static int TIEMPO_EXPIRACION_SESION = 5*60000;  //5 minutos
	
	// Relación entre las sesiones de WebSockets con las de los usuarios
	private static HashMap<String,UUID> sesionesWS;
	
	static {
		sesiones = new HashMap<>();
		sesionesWS = new HashMap<>();
	}
	
	
	public static UUID nuevaSesion(UsuarioVO usuario) {
		UUID sesionID = UUID.randomUUID();
		
		Timer t = new Timer(TIEMPO_EXPIRACION_SESION, new AlarmaSesion(sesionID));
		Sesion sesion = new Sesion(usuario, t);
		
		sesiones.put(sesionID, sesion);
		t.start();
		return sesionID;
	}
	
	public static Sesion obtenerSesion(UUID sesionID) {
		return sesiones.get(sesionID);
	}
	
	public static void eliminarSesion(UUID sesionID) {
		GestorSesiones.obtenerSesion(sesionID).getTimer().stop(); //Para evitar que salte una alarma inexistente
		sesiones.remove(sesionID);
	}
	
	// Sesiones WS
	
	public static void vincularSesionWS(UUID sesionID, String wsSesionID) {
		if (!GestorSesiones.obtenerSesion(sesionID).equals(null)) {
			sesionesWS.put(wsSesionID, sesionID);
		}
	}
	
	public static void desvincularSesionWS(String wsSesionID) {
		sesionesWS.remove(wsSesionID);
	}
	
	public static void desvincularSesionWSyExit(String wsSesionID) {
		UUID sesionID = sesionesWS.get(wsSesionID);
		eliminarSesion(sesionID);
		
		desvincularSesionWS(wsSesionID);
	}
	
	public static boolean haySesionWS(UUID sesionID) {
		return sesionesWS.containsValue(sesionID);
	}
	
	public static UUID obtenerSesionID(String wsSesionID) {
		return sesionesWS.get(wsSesionID);
	}
}
