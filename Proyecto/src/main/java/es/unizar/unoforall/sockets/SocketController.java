package es.unizar.unoforall.sockets;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.gestores.GestorSalas;
import es.unizar.unoforall.gestores.GestorSesiones;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.Serializar;

@Controller
public class SocketController {	
	
	/**
	 * Método para iniciar sesión
	 * @param usrID			En la URL: clave para iniciar sesión obtenida en el login
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				el id de sesión si ha habido éxito, y null en caso contrario
	 * @throws Exception
	 */
	@MessageMapping("/conectarse/{claveInicio}")
	@SendTo("/topic/conectarse/{claveInicio}")
	public String login(@DestinationVariable UUID claveInicio, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
				
		boolean exito = GestorSesiones.iniciarSesion(claveInicio, sesionID); 
		if (exito) {
			System.out.println("Nueva sesión: " + sesionID);
			return sesionID;
		} else {
			return "nulo";
		}
	}
		
	
	/**************************************************************************/
	// Notificaciones
	/**************************************************************************/
	
	/**
	 * Método para enviar una notificación de amistad al usuario con id 
	 * 'usrDestino' si este está susscrito al canal de destino. También
	 * registra la solicitud en la base de datos.
	 * @param usrDestino	En la URL: id del usuario de destino
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase UsuarioVO) El usuario de destino recibirá el VO del emisor
	 * @throws Exception
	 */
	@MessageMapping("/notifAmistad/{usrDestino}")
	@SendTo("/topic/notifAmistad/{usrDestino}")
	public String enviarNotifAmistad(@DestinationVariable UUID usrDestino, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		String error = UsuarioDAO.mandarPeticion(usuarioID,usrDestino);	
		System.err.println(error);
		return Serializar.serializar(UsuarioDAO.getUsuario(usuarioID));
	}
	
	/**
	 * Método para enviar una notificación de invitación a partida al usuario 
	 * con id 'usrDestino' si este está susscrito al canal de destino
	 * @param usrDestino	En la URL: id del usuario de destino
	 * @param sesionID		Automático
	 * @param salaID		ID de la sala a invitar
	 * @return				(Clase NotificacionSala) El usuario de destino recibirá la NotificacionSala 
	 * 						con el id de la sala, y se podrá conectar a esta
	 * 						en /salas/unirse/{salaID}
	 * @throws Exception
	 */
	@MessageMapping("/notifSala/{usrDestino}")
	@SendTo("/topic/notifSala/{usrDestino}")
	public String enviarNotifSala(@DestinationVariable UUID usrDestino, 
							@Header("simpSessionId") String sesionID, 
							UUID salaID) throws Exception {
		return Serializar.serializar(new NotificacionSala(salaID, 
				UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sesionID))));
	}
	
	
	
	/**************************************************************************/
	// Salas
	/**************************************************************************/
	
	/**
	 * Método para unirse a una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala a la que se ha unido el usuario
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@MessageMapping("/salas/unirse/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String unirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Sala("La sala ya no existe"));
		}
		if (GestorSesiones.obtenerUsuarioID(sesionID) == null) {
			return Serializar.serializar(new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
		}
		
		System.out.println(sesionID + " se une a la sala " + salaID);
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipante(UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sesionID)));
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID));
	}
	
	/**
	 * Método para indicar que el usuario está listo para jugar
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@MessageMapping("/salas/listo/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String listoSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Sala("La sala ya no existe"));
		}
		if (GestorSesiones.obtenerUsuarioID(sesionID) == null) {
			return Serializar.serializar(new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
		}
		GestorSalas.obtenerSala(salaID).
			nuevoParticipanteListo(GestorSesiones.obtenerUsuarioID(sesionID));
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID));
	}
	
	/**
	 * Método para salirse de una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						ha sido eliminada
	 * @throws Exception
	 */
	@MessageMapping("/salas/salir/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String salirseSala(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Sala("La sala ya no existe"));
		}
		if (GestorSesiones.obtenerUsuarioID(sesionID) == null) {
			return Serializar.serializar(new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
		}		
		Sala s = GestorSalas.eliminarParticipanteSala(salaID, 
				GestorSesiones.obtenerUsuarioID(sesionID));
		
		if (s == null) {
			return Serializar.serializar(new Sala("La sala se ha eliminado"));
		} else {
			return Serializar.serializar(s);
		}
	}
	
	
	/**************************************************************************/
	// Partidas
	/**************************************************************************/
	
	
	
	
	
	
	
	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		String sesionID = event.getSessionId();
		
		GestorSalas.eliminarParticipanteSalas(GestorSesiones.obtenerUsuarioID(sesionID));
		GestorSesiones.eliminarSesion(sesionID);		
		
		System.err.println("Client disconnected with session id: " + sesionID);
	}
}