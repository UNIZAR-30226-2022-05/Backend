package es.unizar.unoforall.sockets;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import es.unizar.unoforall.apirest.UsuarioDAO;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.salas.GestorSalas;
import es.unizar.unoforall.sesiones.GestorSesiones;

@Controller
public class SocketController {	
	
	/**
	 * Método para iniciar sesión
	 * @param usrID			En la URL: id del usuario
	 * @param sesionID		Automático
	 * @param claveInicio	Clave para iniciar sesión obtenida en el login
	 * @return				True si ha habido éxito y false en caso contrario
	 * @throws Exception
	 */
	@MessageMapping("/conectarse/{usrID}")
	@SendTo("/topic/conectarse/{usrID}")
	public boolean login(@DestinationVariable UUID usrID, 
							@Header("simpSessionId") String sesionID, 
							UUID claveInicio) throws Exception {
		
		System.out.println(usrID + " se ha vinculado a " + sesionID);
		
		boolean exito = GestorSesiones.iniciarSesion(usrID, claveInicio, sesionID);
		
		return exito;
	}
		
	
	/**************************************************************************/
	// Notificaciones
	/**************************************************************************/
	
	/**
	 * Método para enviar una notificación de amistad al usuario con id 
	 * 'usrDestino' si este está susscrito al canal de destino
	 * @param usrDestino	En la URL: id del usuario de destino
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				El usuario de destino recibirá el VO del emisor
	 * @throws Exception
	 */
	@MessageMapping("/enviarNotificacion")
	@SendTo("/topic/notificaciones/{usrDestino}")
	public UsuarioVO enviarNotificacion(@DestinationVariable UUID usrDestino, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		return UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sesionID));
	}
	
	
	
	/**************************************************************************/
	// Salas
	/**************************************************************************/
	
	/**
	 * Método para unirse a una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				La sala a la que se ha unido el usuario
	 * @throws Exception
	 */
	@MessageMapping("/salas/unirse/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala unirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipante(UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sesionID)));
		
		return GestorSalas.obtenerSala(salaID);
	}
	
	/**
	 * Método para indicar que el usuario está listo para jugar
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				La sala actualizada
	 * @throws Exception
	 */
	@MessageMapping("/salas/listo/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala listoSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipanteListo(GestorSesiones.obtenerUsuarioID(sesionID));
		
		return GestorSalas.obtenerSala(salaID);
	}
	
	/**
	 * Método para salirse de una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				La sala actualizada o null si ha sido eliminada
	 * @throws Exception
	 */
	@MessageMapping("/salas/salir/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public Sala salirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
				
		GestorSalas.obtenerSala(salaID).
			eliminarParticipante(GestorSesiones.obtenerUsuarioID(sesionID));
		
		if(GestorSalas.obtenerSala(salaID).numParticipantes() == 0) {
			GestorSalas.eliminarSala(salaID);
			return null;
		} else {
			return GestorSalas.obtenerSala(salaID);
		}
	}
	
	
	
	
	
	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		String sesionID = event.getSessionId();
		GestorSesiones.eliminarSesion(sesionID);
		System.err.println("Client disconnected with session id:" + sesionID);
	}
}