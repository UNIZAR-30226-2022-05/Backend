package es.unizar.unoforall.sockets;

import java.util.Timer;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.gestores.AlarmaTurnoIA;
import es.unizar.unoforall.gestores.GestorSalas;
import es.unizar.unoforall.gestores.GestorSesiones;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.EnvioEmoji;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.Serializar;

@Controller
public class SocketController {	
	
	private final static int DELAY_TURNO_IA = 2*1000;  // 2 segundos
	private final static int DELAY_TURNO_IA_CORTO = 500;  // medio segundo
	
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
	 * 						o null si la petición se la envía a sí mismo
	 * @throws Exception
	 */
	@MessageMapping("/notifAmistad/{usrDestino}")
	@SendTo("/topic/notifAmistad/{usrDestino}")
	public String enviarNotifAmistad(@DestinationVariable UUID usrDestino, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (usuarioID.equals(usrDestino)) {
			return "nulo";
		} else {
			String error = UsuarioDAO.mandarPeticion(usuarioID,usrDestino);	
			System.err.println(error);
			if (error.equals("nulo")) {
				return Serializar.serializar(UsuarioDAO.getUsuario(usuarioID));
			} else {
				return "nulo";
			}
		}
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
		} else if (GestorSalas.obtenerSala(salaID).isEnPartida()) {
			return Serializar.serializar(new Sala("La sala ya está en partida"));
		}
		
		
		if (GestorSesiones.obtenerUsuarioID(sesionID) == null) {
			return Serializar.serializar(new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
		}
		
		System.out.println(sesionID + " se une a la sala " + salaID);
		
		GestorSalas.obtenerSala(salaID).
			nuevoParticipante(UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sesionID)));
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
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
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		sala.nuevoParticipanteListo(GestorSesiones.obtenerUsuarioID(sesionID));
		if(sala.isEnPartida()) {
			GestorSalas.restartTimer(salaID);
		}
				
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
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
			if (s.getPartida() != null && s.getPartida().turnoDeIA()) {
				System.out.println("- - - Preparando turno de la IA");
				AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
				Timer t = new Timer();
				t.schedule(alarm, DELAY_TURNO_IA);
			}
			
			if(s.isEnPartida()) {
				GestorSalas.restartTimer(salaID);
			}
			
			return Serializar.serializar(s.getSalaAEnviar());
		}
	}
	
	
	/**
	 * (EXCLUSIVO BACKEND) Método para avisar a los participantes de la salida
	 * por desconexión de alguno de ellos
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * @throws Exception
	 */
	@MessageMapping("/salas/actualizar/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String actualizarSala(@DestinationVariable UUID salaID,  
							Object vacio) throws Exception {
		
		Sala s = GestorSalas.obtenerSala(salaID);
		
		if (s == null) {
			return Serializar.serializar(new Sala("La sala se ha eliminado"));
		} else {
			if(s.isEnPartida()) {
				GestorSalas.restartTimer(salaID);
			}
			
			return Serializar.serializar(s.getSalaAEnviar());
		}
	}
	
	
	
	/**************************************************************************/
	// Partidas
	/**************************************************************************/
	
	/**
	 * Método para realizar una jugada en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param jugada		Jugada realizada
	 * @return				(Clase Partida) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@MessageMapping("/partidas/turnos/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String turnoPartida(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Jugada jugada) throws Exception {		
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Partida("La sala de la partida ya no existe"));
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
			return Serializar.serializar(new Partida("La partida todavía no ha comenzado"));
		}
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (usuarioID == null) {
			return Serializar.serializar(new Partida("La sesión ha caducado. Vuelva a iniciar sesión"));
		}
		
		System.out.println("- - - " + sesionID + " envia un turno a la sala " + salaID);
		System.out.println("- - - Jugada: " + jugada);
		
		Partida partida = GestorSalas.obtenerSala(salaID).getPartida();
		int turnoAnterior = partida.getTurno();
		partida.ejecutarJugadaJugador(jugada, usuarioID);
		
		if(partida.estaTerminada()) {
			String error = GestorSalas.insertarPartidaEnBd(salaID);
			if (!error.equals("nulo")) {
				//TODO Tratamiento de error al insertar en base de datos
			}	
			GestorSalas.cancelTimer(salaID);
			Sala sala = GestorSalas.obtenerSala(salaID);
			sala.setEnPartida(false);
			
		} else if (partida.turnoDeIA()) {
			System.out.println("- - - Preparando turno de la IA");
			AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
			Timer t = new Timer();
			t.schedule(alarm, DELAY_TURNO_IA);
		}
		
		if(turnoAnterior != partida.getTurno()) {
			GestorSalas.restartTimer(salaID);
		}
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
	}
	
	/**
	 * (EXCLUSIVO BACKEND) Método para avisar de un turno generado por la IA
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Partida) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe
	 * @throws Exception
	 */
	@MessageMapping("/partidas/turnosIA/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String turnoPartidaIA(@DestinationVariable UUID salaID, 
							Object vacio) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Partida("La sala de la partida ya no existe"));
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
			return Serializar.serializar(new Partida("La partida todavía no ha comenzado"));
		}
				
		System.out.println("Una IA envia un turno a la sala " + salaID);
		Partida partida = GestorSalas.obtenerSala(salaID).getPartida();
		int turnoAnterior = partida.getTurno();
		partida.ejecutarJugadaIA();
		
		//Envía un emoji si ha tirado un +4
		if (partida.getUltimaCartaJugada().esDelTipo(Carta.Tipo.mas4)) {
			GestorSesiones.getApiInterna().sendObject("/partidas/emojiPartida/" + salaID, 0);
		}
		
		if(partida.estaTerminada()) {
			String error = GestorSalas.insertarPartidaEnBd(salaID);
			if (!error.equals("nulo")) {
				//TODO Tratamiento de error al insertar en base de datos
			}	
			GestorSalas.cancelTimer(salaID);
			Sala sala = GestorSalas.obtenerSala(salaID);
			sala.setEnPartida(false);
			
		} else if (partida.turnoDeIA()) {
			System.out.println("- - - Preparando turno de la IA");
			AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
			Timer t = new Timer();
			if (partida.isModoJugarCartaRobada()) {
				t.schedule(alarm, DELAY_TURNO_IA_CORTO);
			} else {
				t.schedule(alarm, DELAY_TURNO_IA);
			}
			
		}
		
		if(turnoAnterior != partida.getTurno()) {
			GestorSalas.restartTimer(salaID);
		}
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
	}
	
	
	/**
	 * (EXCLUSIVO BACKEND) Método saltar el turno tras los 30s
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Partida) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe
	 * @throws Exception
	 */
	@MessageMapping("/partidas/saltarTurno/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String saltarTurno(@DestinationVariable UUID salaID, 
							Object vacio) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Partida("La sala de la partida ya no existe"));
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
			return Serializar.serializar(new Partida("La partida todavía no ha comenzado"));
		}
		
		Partida partida = GestorSalas.obtenerSala(salaID).getPartida();
		partida.saltarTurno();
		
		if (partida.turnoDeIA()) {
			System.out.println("- - - Preparando turno de la IA");
			AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
			Timer t = new Timer();
			if (partida.isModoJugarCartaRobada()) {
				t.schedule(alarm, DELAY_TURNO_IA_CORTO);
			} else {
				t.schedule(alarm, DELAY_TURNO_IA);
			}
			
		}
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
	}
	
		
	/**
	 * Método para pulsar el botón de UNO en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Partida) La partida actualizada después de que
	 * 						un jugador presione el botón UNO
	 * 						Partida con 'error' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@MessageMapping("/partidas/botonUNO/{salaID}")
	@SendTo("/topic/salas/{salaID}")
	public String botonUNOPartida(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Object vacio) throws Exception {		
		
		if (GestorSalas.obtenerSala(salaID) == null) {
			return Serializar.serializar(new Partida("La sala de la partida ya no existe"));
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
			return Serializar.serializar(new Partida("La partida todavía no ha comenzado"));
		}
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (usuarioID == null) {
			return Serializar.serializar(new Partida("La sesión ha caducado. Vuelva a iniciar sesión"));
		}
		
		System.out.println("El usuario " + usuarioID + " ha pulsado el botón UNO");
		
		Partida partida = GestorSalas.obtenerSala(salaID).getPartida();
		partida.pulsarBotonUNO(usuarioID);
		
		return Serializar.serializar(GestorSalas.obtenerSala(salaID).getSalaAEnviar());
	}
	
	
	/**
	 * Método para enviar un emoji en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sesionID		Automático
	 * @param emoji			Entero identificador del emoji
	 * @param esIA			falso (solo true cuando lo llame el backend)
	 * @return				(Clase EnvioEmoji) El identificador del emoji y el emisor.
	 * 						El identificador será -1 si ha habido algún error, y hay
	 * 						que ignorar ese envío del emoji
	 * @throws Exception
	 */
	@MessageMapping("/partidas/emojiPartida/{salaID}")
	@SendTo("/topic/salas/{salaID}/emojis")
	public String emojiPartida(@DestinationVariable UUID salaID, 
							@Header("simpSessionId") String sesionID, 
							Integer emoji) throws Exception {		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (usuarioID == null) {	
			//ha mandado el emoji la IA o un usuario no registrado (obviamos eso)
			return Serializar.serializar(new EnvioEmoji(emoji, usuarioID, true));
		} else if (emoji >= 0 && emoji <= 4) {
			return Serializar.serializar(new EnvioEmoji(emoji, usuarioID, false));
		} else {	//emoji inválido
			return Serializar.serializar(new EnvioEmoji(-1, null, false));
		}
	}
	
	
	
	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) throws Exception {
		String sesionID = event.getSessionId();
		
		GestorSalas.eliminarParticipanteSalas(GestorSesiones.obtenerUsuarioID(sesionID));
		GestorSesiones.eliminarSesion(sesionID);		
		
		System.err.println("Client disconnected with session id: " + sesionID);
	}
}