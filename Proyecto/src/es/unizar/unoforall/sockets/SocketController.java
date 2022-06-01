package es.unizar.unoforall.sockets;

import java.util.Timer;
import java.util.UUID;

import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.gestores.AlarmaTurnoIA;
import es.unizar.unoforall.gestores.GestorSalas;
import es.unizar.unoforall.gestores.GestorSesiones;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.EnvioEmoji;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.partidas.RespuestaVotacionPausa;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.model.salas.Sala;
import me.i2000c.web_utils.annotations.PostMapping;
import me.i2000c.web_utils.annotations.RestController;
import me.i2000c.web_utils.annotations.WebsocketController;
import me.i2000c.web_utils.client.RestClient;
import me.i2000c.web_utils.controllers.Controller;
import me.i2000c.web_utils.controllers.DisconnectReason;

@RestController("/app")
@WebsocketController("/topic")
public class SocketController extends Controller{	
    	
	private final static int DELAY_TURNO_IA = 2*1000;  // 2 segundos
	private final static int DELAY_TURNO_IA_CORTO = 500;  // medio segundo

        
        
	
	/**
	 * Método para iniciar sesión
	 * @param usrID			En la URL: clave para iniciar sesión obtenida en el login
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				el id de sesión si ha habido éxito, y null en caso contrario
	 * @throws Exception
	 */
	/*@PostMapping("/conectarse/{claveInicio}")
	public void login(UUID claveInicio, 
							UUID sessionID) throws Exception {
				
		boolean exito = GestorSesiones.iniciarSesion(claveInicio, sessionID); 
		if (exito) {
			System.out.println("Nueva sesión: " + sessionID);
                        super.sendTo("/topic/conectarse/" + claveInicio, sessionID);
			return;
		} else {
			super.sendTo("/topic/conectarse/" + claveInicio, null);
			return;
		}
	}*/
		
	
	/**************************************************************************/
	// Notificaciones
	/**************************************************************************/
	
	/**
	 * Método para enviar una notificación de amistad al usuario con id 
	 * 'usrDestino' si este está susscrito al canal de destino. También
	 * registra la solicitud en la base de datos.
	 * @param usrDestino	En la URL: id del usuario de destino
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase UsuarioVO) El usuario de destino recibirá el VO del emisor
	 * 						o null si la petición se la envía a sí mismo
	 * @throws Exception
	 */
	@PostMapping("/notifAmistad/{usrDestino}")
	public void enviarNotifAmistad(UUID usrDestino, 
							UUID sessionID) throws Exception {
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (usuarioID.equals(usrDestino)) {
                    super.sendTo("/topic/notifAmistad/" + usrDestino, null);
                    return;
		} else {
			String error = UsuarioDAO.mandarPeticion(usuarioID,usrDestino);	
			System.err.println(error);
			if (error == null) {
                            super.sendTo("/topic/notifAmistad/" + usrDestino, UsuarioDAO.getUsuario(usuarioID));
                            return;
			} else {
                            super.sendTo("/topic/notifAmistad/" + usrDestino, null);
                            return;
			}
		}
	}
	
	/**
	 * Método para enviar una notificación de invitación a partida al usuario 
	 * con id 'usrDestino' si este está susscrito al canal de destino
	 * @param usrDestino	En la URL: id del usuario de destino
	 * @param sessionID		Automático
	 * @param salaID		ID de la sala a invitar
	 * @return				(Clase NotificacionSala) El usuario de destino recibirá la NotificacionSala 
	 * 						con el id de la sala, y se podrá conectar a esta
	 * 						en /salas/unirse/{salaID}
	 * @throws Exception
	 */
	@PostMapping("/notifSala/{usrDestino}")
	public void enviarNotifSala(UUID usrDestino, 
							UUID sessionID, 
							UUID salaID) throws Exception {
            super.sendTo("/topic/notifSala/" + usrDestino,
                    new NotificacionSala(salaID, UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sessionID))));
	}
	
	
	
	/**************************************************************************/
	// Salas
	/**************************************************************************/
	
	/**
	 * Método para unirse a una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala a la que se ha unido el usuario
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@PostMapping("/salas/unirse/{salaID}")
	public void unirseSala(UUID salaID, 
							UUID sessionID) throws Exception {
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		if (sala == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala ya no existe"));
                    return;
		}
		
		System.out.println(sessionID + " se une a la sala " + salaID);
		
		sala.nuevoParticipante(UsuarioDAO.getUsuario(GestorSesiones.obtenerUsuarioID(sessionID)));
		
		sala.initAckTimers();
                super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
        
        
	
	/**
	 * Método para indicar que el usuario está listo para jugar
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@PostMapping("/salas/listo/{salaID}")
	public void listoSala(UUID salaID, 
							UUID sessionID) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala ya no existe"));
                    return;
		}
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		sala.nuevoParticipanteListo(GestorSesiones.obtenerUsuarioID(sessionID));
		if(sala.isEnPartida()) {
			GestorSalas.restartTimer(salaID);
			
			if (sala.getPartida().turnoDeIA()) {
				AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
				Timer t = new Timer();
				t.schedule(alarm, DELAY_TURNO_IA);
			}
		}
		sala.initAckTimers();		
		
		super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
	
	/**
	 * Método para salirse de una sala
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						ha sido eliminada
	 * @throws Exception
	 */
	@PostMapping("/salas/salir/{salaID}")
	public void salirseSala(UUID salaID, 
							UUID sessionID) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala ya no existe"));
                    return;
		}
		
		Sala s = GestorSalas.eliminarParticipanteSalaExterno(salaID, 
				GestorSesiones.obtenerUsuarioID(sessionID));
		
		if (s == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala se ha eliminado"));
                    return;
		} else {
			if (s.isEnPartida() && s.getPartida().turnoDeIA()) {
				System.out.println("- - - Preparando turno de la IA");
				AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
				Timer t = new Timer();
				t.schedule(alarm, DELAY_TURNO_IA);
			}
			
			if(s.isEnPartida()) {
				GestorSalas.restartTimer(salaID);
			}
			
			s.initAckTimers();
			super.sendTo("/topic/salas/" + salaID, s.getSalaAEnviar());
		}
	}
	
	
	/**
	 * Método para salirse de una sala en pausa definitivamente (si no, se seguirá
	 * perteneciendo a la partida pausada)
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * 						Sala con 'noExiste' = true si la sala no existe o 
	 * 						ha sido eliminada
	 * @throws Exception
	 */
	@PostMapping("/salas/salirDefinitivo/{salaID}")
	public void salirseSalaDefinitivo(UUID salaID, 
							UUID sessionID) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala ya no existe"));
                    return;
		}	
		
		Sala s = GestorSalas.obtenerSala(salaID);
		s.eliminarParticipanteDefinitivamente(GestorSesiones.obtenerUsuarioID(sessionID));
		
		if(s.numParticipantes() == 0) {
			System.out.println("Eliminando sala " + salaID);
			GestorSalas.eliminarSala(salaID);
			super.sendTo("/topic/salas/" + salaID, new Sala("La sala se ha eliminado"));
                        return;
		}
		s.initAckTimers();		
		super.sendTo("/topic/salas/" + salaID, s.getSalaAEnviar());
	}
	
	
	/**
	 * (EXCLUSIVO BACKEND) Método para avisar a los participantes de la salida
	 * por desconexión de alguno de ellos
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La sala actualizada
	 * @throws Exception
	 */
	@PostMapping("/salas/actualizar/{salaID}")
	public void actualizarSala(UUID salaID) throws Exception {
		Sala s = GestorSalas.obtenerSala(salaID);
		
		if (s == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala se ha eliminado"));
                    return;
		} else {
			if(s.isEnPartida()) {
				GestorSalas.restartTimer(salaID);
				s.getPartida().resetUltimaJugada(); 
			}
			
			s.initAckTimers();
			super.sendTo("/topic/salas/" + salaID, s.getSalaAEnviar());
		}
	}
	
	
	
	/**************************************************************************/
	// Partidas
	/**************************************************************************/
	
	/**
	 * Método para realizar una jugada en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param jugada		Jugada realizada
	 * @return				(Clase Sala) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@PostMapping("/partidas/turnos/{salaID}")
	public void turnoPartida(UUID salaID, 
							UUID sessionID, 
							Jugada jugada) throws Exception {		
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala de la partida ya no existe"));
                    return;
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La partida todavía no ha comenzado"));
                    return;
		}  else if (GestorSalas.obtenerSala(salaID).isEnPausa()) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La partida está en pausa"));
                    return;
		}
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (usuarioID == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
                    return;
		}
		
		
		System.out.println("- - - " + sessionID + " envia un turno a la sala " + salaID);
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		Partida partida = sala.getPartida();
		
		if (partida.turnoDeIA()) {
			System.out.println("- - - Jugada en turno incorrecto");
                        super.sendTo("/topic/salas/" + salaID, GestorSalas.obtenerSala(salaID).getSalaAEnviar());
                        return;
		}
		
		int turnoAnterior = partida.getTurno();
		boolean jugadaValida = partida.ejecutarJugadaJugador(jugada, usuarioID);
		
		if (!jugadaValida) {
			System.out.println("- - - Jugada inválida");
                        super.sendTo("/topic/salas/" + salaID, GestorSalas.obtenerSala(salaID).getSalaAEnviar());
                        return;
		} else {
			System.out.println("- - - Jugada: " + jugada);
		}
		
		if(partida.estaTerminada()) {
			String error = GestorSalas.insertarPartidaEnBd(salaID);
			if (error != null) {
				System.err.println("Error al insertar la partida en la BD");
			}	
			GestorSalas.cancelTimer(salaID);
			sala.setEnPartidaExterno(false);
			
		} else {
			if (partida.turnoDeIA()) {
				System.out.println("- - - Preparando turno de la IA");
				AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
				Timer t = new Timer();
				t.schedule(alarm, DELAY_TURNO_IA);
			}
			
			if(turnoAnterior != partida.getTurno() || partida.isRepeticionTurno()) {
				GestorSalas.restartTimer(salaID);
			}
		}
		sala.initAckTimers();
                super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
	
	/**
	 * (EXCLUSIVO BACKEND) Método para avisar de un turno generado por la IA
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe
	 * @throws Exception
	 */
	@PostMapping("/partidas/turnosIA/{salaID}")
	public void turnoPartidaIA(UUID salaID) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala de la partida ya no existe"));
                    return;
		} else if (GestorSalas.obtenerSala(salaID).isEnPausa()) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La partida está pausada"));
                    return;
		}
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		Partida partida = sala.getPartida();
		int turnoAnterior = partida.getTurno();
		partida.ejecutarJugadaIA();
		
		Jugada ultimaJugada = partida.getUltimaJugada();
		//Envía un emoji si ha tirado un +4
		if (ultimaJugada != null
			&& ultimaJugada.getCartas() != null
			&& ultimaJugada.getCartas().size() == 1
			&& ultimaJugada.getCartas().get(0).esDelTipo(Carta.Tipo.mas4)) {
                    RestClient client = GestorSesiones.getApiInterna().getRestClient();
                    client.addParameter("emoji", new EnvioEmoji(0,turnoAnterior,true));
                    client.openConnection("/app/partidas/emojiPartida/" + salaID);
                    client.receiveObject(String.class, null);
		}
		
		if(partida.estaTerminada()) {
			String error = GestorSalas.insertarPartidaEnBd(salaID);
			if (error != null) {
				System.err.println("Error al insertar la partida en la BD");
			}	
			GestorSalas.cancelTimer(salaID);
			sala.setEnPartidaExterno(false);
			
		} else {
			
			if (partida.turnoDeIA()) {
				AlarmaTurnoIA alarm = new AlarmaTurnoIA(salaID);
				Timer t = new Timer();
				if (partida.isModoJugarCartaRobada()) {
					t.schedule(alarm, DELAY_TURNO_IA_CORTO);
				} else {
					t.schedule(alarm, DELAY_TURNO_IA);
				}
			}
			
			if(turnoAnterior != partida.getTurno() || partida.isRepeticionTurno()) {
				GestorSalas.restartTimer(salaID);
			}
		}
		sala.initAckTimers();
                super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
	
	
	/**
	 * (EXCLUSIVO BACKEND) Método saltar el turno tras los 30s
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La partida actualizada tras cada turno
	 * 						Partida con 'error' = true si la sala no existe
	 * @throws Exception
	 */
	@PostMapping("/partidas/saltarTurno/{salaID}")
	public void saltarTurno(UUID salaID) throws Exception {
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala de la partida ya no existe"));
                    return;
		}  else if (GestorSalas.obtenerSala(salaID).isEnPausa()) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La partida está en pausa..."));
                    return;
		}
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		Partida partida = sala.getPartida();
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
		
		GestorSalas.restartTimer(salaID);
		sala.initAckTimers();
                super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
	
		
	/**
	 * Método para pulsar el botón de UNO en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				(Clase Sala) La partida actualizada después de que
	 * 						un jugador presione el botón UNO
	 * 						Partida con 'error' = true si la sala no existe o 
	 * 						el usuario no está logueado
	 * @throws Exception
	 */
	@PostMapping("/partidas/botonUNO/{salaID}")
	public void botonUNOPartida(UUID salaID, 
							UUID sessionID) throws Exception {		
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sala de la partida ya no existe"));
                    return;
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La partida todavía no ha comenzado"));
                    return;
		}
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (usuarioID == null) {
                    super.sendTo("/topic/salas/" + salaID, new Sala("La sesión ha caducado. Vuelva a iniciar sesión"));
                    return;
		}
		
		System.out.println("El usuario " + usuarioID + " ha pulsado el botón UNO");
		
		Sala sala = GestorSalas.obtenerSala(salaID);
		Partida partida = sala.getPartida();
		partida.pulsarBotonUNO(usuarioID);
		
		sala.initAckTimers();
                super.sendTo("/topic/salas/" + salaID, sala.getSalaAEnviar());
	}
	
	
	/**
	 * Método para enviar un emoji en una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param emoji			Entero identificador del emoji
	 * @param esIA			falso (solo true cuando lo llame el backend)
	 * @return				(Clase EnvioEmoji) El identificador del emoji y el emisor
	 * @throws Exception
	 */
	@PostMapping("/partidas/emojiPartida/{salaID}")
	public void emojiPartida(UUID salaID, 
							UUID sessionID, 
							EnvioEmoji emoji) throws Exception {
            super.sendTo("/topic/salas/" + salaID + "/emojis", emoji);
	}
	
	
	/**
	 * Método para votar el pausado de una partida
	 * @param salaID		En la URL: id de la sala
	 * @param sessionID		Automático
	 * @param vacio			Cualquier objeto no nulo
	 * @return				Clase RespuestaVotacionPausa
	 * 						nulo si la sala no existe, no está en partida, o el
	 * 						emisor no tiene una sesión iniciada
	 * @throws Exception
	 */
	@PostMapping("/partidas/votaciones/{salaID}")
	public void votacionPartida(UUID salaID, 
							UUID sessionID) throws Exception {	
		
		if (GestorSalas.obtenerSala(salaID) == null) {
                    super.sendTo("/topic/salas/" + salaID + "/votaciones", null);
                    return;
		} else if (!GestorSalas.obtenerSala(salaID).isEnPartida()) {
                    super.sendTo("/topic/salas/" + salaID + "/votaciones", null);
                    return;
		}
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (usuarioID == null) {
                    super.sendTo("/topic/salas/" + salaID + "/votaciones", null);
                    return;
		}
		
		RespuestaVotacionPausa resp = GestorSalas.obtenerSala(salaID)
										.setParticipantesVotoAbandono(usuarioID);
		
		if (resp.getNumVotos() == resp.getNumVotantes()) {
                    RestClient client = GestorSesiones.getApiInterna().getRestClient();
                    client.openConnection("/app/salas/actualizar/" + salaID);
                    client.receiveObject(String.class, null);
		}
		
		super.sendTo("/topic/salas/" + salaID + "/votaciones", resp);
	}
	
	
	/**
	 * (EXCLUSIVO BACKEND) Método para actualizar los votos cuando un jugador abandona
	 * @param salaID		En la URL: id de la sala
	 * @param vacio			Cualquier objeto no nulo
	 * @return				Clase RespuestaVotacionPausa
	 * 						nulo si la sala no existe, no está en partida, o el
	 * 						emisor no tiene una sesión iniciada
	 * @throws Exception
	 */
	@PostMapping("/partidas/votacionesInternas/{salaID}")
	public void votacionPartidaInterna(UUID salaID) throws Exception {	
		Sala sala = GestorSalas.obtenerSala(salaID);
		if (sala == null) {
                    super.sendTo("/topic/salas/" + salaID + "/votaciones", null);
                    return;
		} else if (!sala.isEnPartida()) {
                    super.sendTo("/topic/salas/" + salaID + "/votaciones", null);
                    return;
		}
		
		super.sendTo("/topic/salas/" + salaID + "/votaciones", sala.getParticipantesVotoAbandonoExterno());
	}
	
	@Override
        public void onDisconnect(UUID sessionID, DisconnectReason reason) {
            //SessionHandler.logout(sessionID);
            GestorSalas.eliminarParticipanteSalas(GestorSesiones.obtenerUsuarioID(sessionID));
            GestorSesiones.eliminarSesion(sessionID);	

            System.out.println("Client disconnected with session id: " + sessionID + "; Reason: " + reason);
        }
	
	/*@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) throws Exception {
		String sessionID = event.getSessionId();
		
		SessionHandler.logout(sessionID);
		GestorSalas.eliminarParticipanteSalas(GestorSesiones.obtenerUsuarioID(sessionID));
		GestorSesiones.eliminarSesion(sessionID);	
		
		System.out.println("Client disconnected with session id: " + sessionID);
	}
	
	public static void desconectarUsuario(String sessionID) {
		SessionHandler.logout(sessionID);
	}*/
	
	public static void desconectarUsuario(UUID usuarioID) {
		UUID sessionID = GestorSesiones.obtenerSesionID(usuarioID);
		if (sessionID != null) {
			desconectarUsuario(sessionID);
		}
	}

    
}