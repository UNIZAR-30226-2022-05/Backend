package es.unizar.unoforall.sockets;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.salas.GestorSalas;
import es.unizar.unoforall.sesiones.GestorSesiones;
import es.unizar.unoforall.sesiones.Sesion;

@Controller
public class SocketController {	
	
//	/**
//	 * Método para iniciar sesión
//	 * @param salaID		En la URL: id de la sala
//	 * @param wsSesionId	Automático
//	 * @param sesionID		ID de la sesión del usuario
//	 * @return				La sala a la que se ha unido el usuario
//	 * @throws Exception
//	 */
//	@MessageMapping("/login/{sesionID}")
//	@SendTo("/topic/login/{sesionID}")
//	public Object login(@DestinationVariable UUID usrID, 
//							@Header("simpSessionId") String wsSesionId, 
//							UUID sesionID) throws Exception {
//		
//		System.out.println(wsSesionId + " se ha vinculado a " + sesionID);
//		GestorSesiones.vincularSesionWS(sesionID, wsSesionId);
//		
//		return null;
//	}
	
	
	/**
	 * Método para unirse a una sala
	 * @param salaID		En la URL: id de la sala
	 * @param wsSesionId	Automático
	 * @param sesionID		ID de la sesión del usuario
	 * @return				La sala a la que se ha unido el usuario
	 * @throws Exception
	 */
	@MessageMapping("/salas/unirse/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala unirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String wsSesionId, 
							UUID sesionID) throws Exception {
		
		System.out.println(wsSesionId + " se ha vinculado a " + sesionID);
		GestorSesiones.vincularSesionWS(sesionID, wsSesionId);
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipante(GestorSesiones.obtenerSesion(sesionID).getUsuario());
		
		return GestorSalas.obtenerSala(salaID);
	}
	
	
	
	/**
	 * 
	 * @param salaID
	 * @param wsSesionId
	 * @param vacio
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/salas/listo/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala listoSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String wsSesionId, 
							Object vacio) throws Exception {
		UUID sesionID = GestorSesiones.obtenerSesionID(wsSesionId);
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipanteListo(GestorSesiones.obtenerSesion(sesionID).getUsuario().getId());
		
		return GestorSalas.obtenerSala(salaID);
	}
	
	
	
	/**
	 * 
	 * @param salaID
	 * @param wsSesionId
	 * @param vacio
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/salas/salir/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala salirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String wsSesionId, 
							Object vacio) throws Exception {
		
		GestorSesiones.desvincularSesionWS(wsSesionId);
		
		UUID sesionID = GestorSesiones.obtenerSesionID(wsSesionId);
		GestorSalas.obtenerSala(salaID).
			eliminarParticipante(GestorSesiones.obtenerSesion(sesionID).getUsuario().getId());
		
		
		if(GestorSalas.obtenerSala(salaID).numParticipantes() == 0) {
			GestorSalas.eliminarSala(salaID);
			return null;
		} else {
			return GestorSalas.obtenerSala(salaID);
		}
	}
	
	
	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		String wsSesionId = event.getSessionId();
		GestorSesiones.desvincularSesionWSyExit(wsSesionId);
		System.err.println("Client disconnected with session id:" + wsSesionId);
	}
}