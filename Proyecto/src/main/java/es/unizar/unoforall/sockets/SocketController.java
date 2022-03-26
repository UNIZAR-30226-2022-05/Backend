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

@Controller
public class SocketController {	
	
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
	
	@MessageMapping("/salas/salir/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala salirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String wsSesionId, 
							Object vacio) throws Exception {
		
		GestorSesiones.desvincularSesionWS(wsSesionId);
		return GestorSalas.obtenerSala(salaID);
	}
	
	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		String wsSesionId = event.getSessionId();
		GestorSesiones.desvincularSesionWS(wsSesionId);
		System.err.println("Client disconnected with session id:" + wsSesionId);
	}
}