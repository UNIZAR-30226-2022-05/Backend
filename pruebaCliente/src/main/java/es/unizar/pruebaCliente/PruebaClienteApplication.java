package es.unizar.pruebaCliente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.unizar.unoforall.model.*;
import es.unizar.unoforall.model.salas.*;
import es.unizar.unoforall.model.partidas.*;
import es.unizar.pruebaCliente.utils.HashUtils;
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;
import es.unizar.unoforall.model.salas.RespuestaSala;
import es.unizar.unoforall.model.salas.Sala;


@SpringBootApplication
public class PruebaClienteApplication {
	
	private static Object LOCK = new Object();
	private static String sesionID = "EMPTY";
	
	private static Partida miPartida = null;
	private static UUID miID = null;
	private static String miSala = null;
	
	
	public static class RespuestaSalas {
		private HashMap<UUID,Sala> salas;
		
		public RespuestaSalas(HashMap<UUID,Sala> salas) {
			this.setSalas(salas);
		}

		public HashMap<UUID,Sala> getSalas() {
			return salas;
		}

		public void setSalas(HashMap<UUID,Sala> salas) {
			this.salas = salas;
		}
	}
	
	

	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Correo");
		String correo = scanner.nextLine();
		System.out.println("Contraseña");
		String contrasenna = scanner.nextLine();
		
		///LOGIN
		String contrasennaHash = HashUtils.cifrarContrasenna(contrasenna);
		System.out.println("El hash de la contraseña es: " + contrasennaHash);
		
		RestAPI apirest = new RestAPI("/api/login");
		apirest.addParameter("correo", correo);
		apirest.addParameter("contrasenna", contrasennaHash);
		apirest.setOnError(e -> {System.out.println(e);});
    	
		apirest.openConnection();
    	RespuestaLogin resp = apirest.receiveObject(RespuestaLogin.class);
    	
    	while(!resp.isExito()) {
    		System.out.println("Correo");
    		correo = scanner.nextLine();
    		System.out.println("Contraseña");
    		contrasenna = scanner.nextLine();
    		
    		apirest = new RestAPI("/api/login");
    		apirest.addParameter("correo", correo);
    		apirest.addParameter("contrasenna", contrasenna);
    		apirest.setOnError(e -> {System.out.println(e);});
        	
    		apirest.openConnection();
        	resp = apirest.receiveObject(RespuestaLogin.class);
    	}
    	System.out.println("clave inicio: " + resp.getClaveInicio());
    	
    	miID = resp.getUsuarioID();
		
				
		//CONEXION
		WebSocketAPI api = new WebSocketAPI();
    	
    	api.openConnection();
    	
    	api.subscribe("/topic/conectarse/" + resp.getClaveInicio(), String.class, s -> {
    		if (s == null) {
    			System.out.println("Error al iniciar sesión (se queda bloqueado el cliente)");
    		} else {
    			sesionID = s;
    			System.out.println("ID sesión: " + sesionID);
	    		synchronized (LOCK) {
					LOCK.notify();
				}
    		}
    	});
    	
    	api.sendObject("/app/conectarse/" + resp.getClaveInicio(), "vacio");
		
    	System.out.println("Esperando inicio sesión... ");
		synchronized (LOCK) {
			LOCK.wait();
		}
		System.out.println("Sesión iniciada");
    	
    	
    	//NOTIFICACIONES
    	api.subscribe("/topic/notifAmistad/" + resp.getUsuarioID(), UsuarioVO.class, remitente -> {
    		System.out.println("Solicitud recibida de: " + remitente);
    	});
    	api.subscribe("/topic/notifSala/" + resp.getUsuarioID(), NotificacionSala.class, notif -> {
    		System.out.println("Invitación de: " + notif.getRemitente() + " a la sala " + notif.getSalaID());
    	});
		
    	
    	ReglasEspeciales reglas = new ReglasEspeciales(false, false, false, false, false, false, true);
    	ConfigSala config = new ConfigSala(ConfigSala.ModoJuego.Original, reglas, 2, true);
		
    	//ACCIONES
			while(true) {
				System.out.println("Introduce una orden: ");
				String orden = scanner.nextLine();
				
				if (orden.equals("crear")) {
					System.out.println("Creando sala");
					///CREAR SALA					
			    	apirest = new RestAPI("/api/crearSala");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("configuracion", config);
					apirest.setOnError(e -> {System.out.println(e);});
			    	
					apirest.openConnection();
					RespuestaSala salaID = apirest.receiveObject(RespuestaSala.class);
					if (salaID.isExito()) {
						System.out.println("sala creada:" + salaID.getSalaID());
					} else {
						System.out.println("error:" + salaID.getErrorInfo());
					}
					
					
				} else if (orden.equals("buscar")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					
					apirest = new RestAPI("/api/buscarSalaID");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("salaID", salaID);
					apirest.setOnError(e -> {System.out.println(e);});
			    	
					apirest.openConnection();
					Sala r = apirest.receiveObject(Sala.class);
					
					if (r.isNoExiste()) {
		    			System.out.println("No se ha encontrado ninguna sala");
		    		} else {
		    			System.out.println("sala encontrada:" + r);
		    		}
				
					
				} else if (orden.equals("filtrar")) {
					apirest = new RestAPI("/api/filtrarSalas");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("configuracion", new ConfigSala(ConfigSala.ModoJuego.Original, new ReglasEspeciales(), 4, true));
					apirest.setOnError(e -> {System.out.println(e);});
			    	
					apirest.openConnection();
					RespuestaSalas r = apirest.receiveObject(RespuestaSalas.class);
					
					r.getSalas().forEach((k,v) -> System.out.println("key: "+k+" value:"+v));			
					
				
				} else if (orden.equals("amigo")) {		//solo comprueba la función de websockets
					System.out.println("Introduce id usuario amigo:");
					String usuarioID = scanner.nextLine();
					
					api.sendObject("/app/notifAmistad/" + usuarioID, "vacio");
				
					
				} else if (orden.equals("notif sala")) {	
					System.out.println("Introduce id usuario amigo:");
					String usuarioID = scanner.nextLine();
					
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					
					api.sendObject("/app/notifSala/" + usuarioID, UUID.fromString(salaID));
				
				} else if (orden.equals("modificarCuenta")) {
					System.out.println("Introduzca su nuevo nombre:");
					String nuevoNombre = scanner.nextLine();
					System.out.println("Introduzca su nueva contraseña:");
					String nuevaContrasenna = scanner.nextLine();
					System.out.println("Introduzca su nuevo correo:");
					String nuevoCorreo = scanner.nextLine();
					
					apirest = new RestAPI("/api/actualizarCuentaStepOne");
					apirest.addParameter("sesionID",sesionID);
					apirest.addParameter("correoNuevo",nuevoCorreo);
					apirest.addParameter("nombre",nuevoNombre);
					apirest.addParameter("contrasenna",nuevaContrasenna);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	String retorno = apirest.receiveObject(String.class);
			    	if (retorno == null) {
			    		System.out.println("Introduzca el código:");
						//Integer codigo = Integer.valueOf(scanner.nextLine());
			    		String codigo = scanner.nextLine();
						
						apirest = new RestAPI("/api/actualizarCuentaStepTwo");
						apirest.addParameter("sesionID",sesionID);
						apirest.addParameter("codigo",codigo);
						apirest.setOnError(e -> {System.out.println(e);});
						
						apirest.openConnection();
				    	retorno = apirest.receiveObject(String.class);
				    	if (retorno == null) {
				    		System.out.println("Exito.");
				    	} else {
				    		System.out.println(retorno);
				    	}
			    	} else {
			    		System.out.println(retorno);
			    	}
			    	
				} else if (orden.equals("sacarID")) {
					apirest = new RestAPI("/api/sacarUsuarioVO");
					apirest.addParameter("sesionID", sesionID);
					apirest.openConnection();
					UsuarioVO usuario = apirest.receiveObject(UsuarioVO.class);
					System.out.println("Tu ID de usuario es: " + usuario.getId());
					
				} else if (orden.equals("buscarAmigo")) {
					System.out.println("Introduce correo usuario amigo:");
					String correoAmigo = scanner.nextLine();
					
					apirest = new RestAPI("/api/buscarAmigo");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("amigo", correoAmigo);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaUsuarios retorno = apirest.receiveObject(ListaUsuarios.class);
			    	if(retorno.isExpirado()) {
			    		System.out.println("La sesión ha expirado.");
			    	} else if (retorno.isExpirado()) {
			    		System.out.println("Ha sucedido el siguiente error:"+retorno.getError());
			    	} else {
			    		System.out.println("Datos del usuario: ");
			    		System.out.println("ID: "+ retorno.getUsuarios().get(0).getId());
			    		System.out.println("Correo: "+ retorno.getUsuarios().get(0).getCorreo());
			    		System.out.println("Nombre: "+ retorno.getUsuarios().get(0).getNombre());
			    		System.out.println("Puntos: "+ retorno.getUsuarios().get(0).getPuntos());
			    	}
			    		
				} else if (orden.equals("aceptarPeticion")) {
				    apirest = new RestAPI("/api/sacarPeticionesRecibidas");
					apirest.addParameter("sesionID", sesionID);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaUsuarios retorno = apirest.receiveObject(ListaUsuarios.class);
			    	if(retorno.isExpirado()) {
			    		System.out.println("La sesión ha expirado.");
			    	} else if (retorno.getError().equals(null)) {
			    		System.out.println("Ha sucedido el siguiente error: "+retorno.getError());
			    	} else {
			    		System.out.println("Introduzca el código que desea aceptar: ");
			    		for (UsuarioVO usuario : retorno.getUsuarios()) {
				    		System.out.println("Datos del usuario: "+usuario.getId());
			    		}
			    		String identificador = scanner.nextLine();
			    		apirest = new RestAPI("/api/aceptarPeticionAmistad");
						apirest.addParameter("sesionID", sesionID);
						apirest.addParameter("amigo", identificador);
						apirest.setOnError(e -> {System.out.println(e);});
						
						apirest.openConnection();
				    	String error = apirest.receiveObject(String.class);
				    	if(error==null) {
				    		System.out.println("¡Ya sois amigos!");
				    	} else {
				    		System.out.println("Ha surgido un error: " + error);
				    	}
			    	}
				} else if (orden.equals("cancelarPeticion")) {
					System.out.println("Introduce el ID del usuario de la petición:");
					String idAmigoPeticion = scanner.nextLine();
					
				    apirest = new RestAPI("/api/cancelarPeticionAmistad");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("amigo", idAmigoPeticion);
					apirest.setOnError(e -> {System.out.println(e);});
					apirest.openConnection();
					String error = apirest.receiveObject(String.class);
					System.out.println("error: " + error);
				} else if (orden.equals("peticionesEnviadas")) {
					
					apirest = new RestAPI("/api/sacarPeticionesEnviadas");
					apirest.addParameter("sesionID", sesionID);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaUsuarios retorno = apirest.receiveObject(ListaUsuarios.class);
			    	if(retorno.isExpirado()) {
			    		System.out.println("La sesión ha expirado.");
			    	} else if (retorno.getError().equals(null)) {
			    		System.out.println("Ha sucedido el siguiente error: "+retorno.getError());
			    	} else {
			    		for (UsuarioVO usuario : retorno.getUsuarios()) {
				    		System.out.println("Datos del usuario: ");
				    		System.out.println("Correo: "+ usuario.getCorreo());
				    		System.out.println("Nombre: "+ usuario.getNombre());
				    		System.out.println("Puntos: "+ usuario.getPuntos());
			    		}
			    	}
				} else if (orden.equals("peticionesRecibidas")) {
					
					apirest = new RestAPI("/api/sacarPeticionesRecibidas");
					apirest.addParameter("sesionID", sesionID);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaUsuarios retorno = apirest.receiveObject(ListaUsuarios.class);
			    	if(retorno.isExpirado()) {
			    		System.out.println("La sesión ha expirado.");
			    	} else if (retorno.getError().equals(null)) {
			    		System.out.println("Ha sucedido el siguiente error: "+retorno.getError());
			    	} else {
			    		for (UsuarioVO usuario : retorno.getUsuarios()) {
				    		System.out.println("Datos del usuario: ");
				    		System.out.println("Correo: "+ usuario.getCorreo());
				    		System.out.println("Nombre: "+ usuario.getNombre());
				    		System.out.println("Puntos: "+ usuario.getPuntos());
			    		}
			    	}
				} else if (orden.equals("sacarAmigos")) {
					apirest = new RestAPI("/api/sacarAmigos");
					apirest.addParameter("sesionID", sesionID);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaUsuarios retorno = apirest.receiveObject(ListaUsuarios.class);
			    	if(retorno.isExpirado()) {
			    		System.out.println("La sesión ha expirado.");
			    	} else if (retorno.getError().equals(null)) {
			    		System.out.println("Ha sucedido el siguiente error: "+retorno.getError());
			    	} else {
			    		for (UsuarioVO usuario : retorno.getUsuarios()) {
				    		System.out.println("Datos del usuario: ");
				    		System.out.println("Correo: "+ usuario.getCorreo());
				    		System.out.println("Nombre: "+ usuario.getNombre());
				    		System.out.println("Puntos: "+ usuario.getPuntos());
			    		}
			    	}
				} else if (orden.equals("reestablecerContrasenna")) {
					System.out.println("Introduce tu correo:");
					String miCorreo = scanner.nextLine();
					
					apirest = new RestAPI("/api/reestablecerContrasennaStepOne");
					apirest.addParameter("correo",miCorreo);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	String retorno = apirest.receiveObject(String.class);
			    	if(retorno == null) {
			    		System.out.println("Introduce el código:");
						Integer codigo = Integer.valueOf(scanner.nextLine());
						
						apirest = new RestAPI("/api/reestablecerContrasennaStepTwo");
						apirest.addParameter("correo",miCorreo);
						apirest.addParameter("codigo", codigo);
						apirest.setOnError(e -> {System.out.println(e);});
						
						apirest.openConnection();
				    	retorno = apirest.receiveObject(String.class);
				    	if (retorno == null) {
							 System.out.println("Introduce la nueva contraseña:");
							 String miContrasenna = scanner.nextLine();
							 apirest = new RestAPI("/api/reestablecerContrasennaStepThree");
							 apirest.addParameter("correo",miCorreo);
							 apirest.addParameter("contrasenna", miContrasenna);
							 apirest.setOnError(e -> {System.out.println(e);});
							
							 apirest.openConnection();
					    	 retorno = apirest.receiveObject(String.class);
					    	 if (retorno == null){
					    		 System.out.println("Contraseña reestablecida.");
					    	 } else {
					    	 	System.out.println(retorno);
					    	 }
								
						 } else {
							 System.out.println(retorno);
						 }
			    	} else {
			    		System.out.println(retorno);
			    	}
										
				} else if (orden.equals("borrarCuenta")) {
					
					apirest = new RestAPI("/api/borrarCuenta");
					apirest.addParameter("sesionID",sesionID);
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	String retorno = apirest.receiveObject(String.class);
			    	System.out.println(retorno);	
				} else if (orden.equals("insertarPartidaAcabada")) {
					
					apirest = new RestAPI("/api/insertarPartidaAcabadaTEST");
					apirest.setOnError(e -> {System.out.println(e);});
					apirest.openConnection();
			    	String retorno = apirest.receiveObject(String.class);
			    	System.out.println(retorno);	
				}
				
				
				
				
				
				
				
				
				else if (orden.equals("unirse")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					miSala = salaID;
					
			    	api.subscribe("/topic/salas/" + salaID, Sala.class, s -> {
			    		System.out.println("Sala actualizada");
			    		if (s.isNoExiste()) {
			    			System.out.println("Error al conectarse a la sala");
			    			api.unsubscribe("/topic/salas/" + salaID);
			    		} else {
			    			System.out.println("Estado de la sala: " + s);
			    			
			    			if (s.isEnPartida() && miPartida == null) {
			    				// acaba de comenzar la partida
			    				
			    				miPartida = s.getPartida();
			    				System.out.println(miPartida);
			    			}
			    		}
			    	});
			    	
			    	api.sendObject("/app/salas/unirse/" + salaID, "vacio");
			    	
			    	
				} else if (orden.equals("listo")) {
					//System.out.println("Introduce id sala:");
					//String salaID = scanner.nextLine();
					String salaID = miSala;
					api.sendObject("/app/salas/listo/" + salaID, "vacio");
					
					
				} else if (orden.equals("salir")) {
					//System.out.println("Introduce id sala:");
					//String salaID = scanner.nextLine();
					String salaID = miSala;
					
					api.sendObject("/app/salas/salir/" + salaID, "vacio");
					api.unsubscribe("/topic/salas/" + salaID);
				
				
				
				
				
				
				} else if (orden.equals("unirsePartida")) {
					//System.out.println("Introduce id:");
					//String salaID = scanner.nextLine();
					String salaID = miSala;
					
			    	api.subscribe("/topic/partidas/turnos/" + salaID, Partida.class, p -> {
			    		if (p.isHayError()) {
			    			System.out.println("Error al enviar turno: " + p.getError());
			    			api.unsubscribe("/topic/partidas/turnos/" + salaID);
			    		} else {
			    			miPartida = p;
			    			System.out.println("-------------------------- Nuevo turno\n" + p);
			    		}
			    	});
			    	
			    	api.subscribe("/topic/salas/" + salaID + "/emojis", EnvioEmoji.class, p -> {
			    		System.out.println("Emoji: " + p.getEmoji());
			    	});
			    	
			    	
				} else if (orden.equals("turno")) {
					//System.out.println("Introduce id:");
					//String salaID = scanner.nextLine();
					String salaID = miSala;
					
					if (miPartida.getJugadorActual().getJugadorID().equals(miID)) {
						System.out.println("Introduce la carta:");
						Integer carta = Integer.valueOf(scanner.nextLine());
						
						ArrayList<Carta> lista = new ArrayList<Carta>();
						lista.add(miPartida.getJugadorActual().getMano().get(carta));
						Jugada j = new Jugada(lista);
						System.out.println("- -  - - Jugada:" + j);
						
						api.sendObject("/app/partidas/turnos/" + salaID, j);
					} else {
						System.err.println("No es tu turno");
					}
				
				
				} else if (orden.equals("enviarEmoji")) {
					String salaID = miSala;
					api.sendObject("/app/partidas/emojiPartida/" + salaID, new EnvioEmoji(1, 2, false));
				
				
				
				}	else if (orden.equals("doramion")){ //Extrae info de las partidas jugadas del usuario
					UUID usuario = UUID.fromString("4c2a49ed-48be-4970-9010-edb1faf918f1");
					apirest = new RestAPI("/api/sacarPartidasJugadas");
					apirest.addParameter("sesionID", sesionID);
					
					apirest.setOnError(e -> {System.out.println(e);});
					
					apirest.openConnection();
			    	ListaPartidas retorno = apirest.receiveObject(ListaPartidas.class);
			    	if (retorno.getError().equals("nulo")) {
			    		for(PartidaJugada partida : retorno.getPartidas()) {
			    			for (Participante p : partida.getParticipantes()) {
			    				System.out.println("Participante: "+p.getUsuario().getId());
			    				System.out.println("Puesto: "+p.getPuesto());
			    				System.out.println("Puntos: "+p.getPuntos());
			    			}
			    		}
			    	} else {
			    		System.out.println(retorno.getError());
			    	}
				} else if (orden.equals("exit")) {
					break;
				}
		}
			
		
		

		
    	
    	
		//SpringApplication.run(PruebaClienteApplication.class, args);
		
//		Empleado[] l = restTemplate().getForObject(
//				"http://localhost/api/empleados", Empleado[].class);
//		
//		System.out.println(l[1].sueldo);
//		Arrays.asList(l).forEach(e -> System.out.println(e.nombre + " " + e.sueldo));

		
		
//		String sss = restTemplate().getForObject(
//		"http://localhost/api/empleados", String.class);
//
//		System.out.println(sss);
		
		
		
//		WebSocketClient client = new StandardWebSocketClient();
//
//		WebSocketStompClient stompClient = new WebSocketStompClient(client);
//		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//		StompSessionHandler sessionHandler = new StompSessionHandler() {
//			@Override
//			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//				synchronized (LOCK) {
//					LOCK.notify();
//				}
//			}
//			@Override
//			public void handleFrame(StompHeaders headers, Object payload) {
//				String e = (String) payload;
//			    System.out.println(e);
//			}
//			@Override
//			public Type getPayloadType(StompHeaders headers) {
//				return String.class;
//			}
//			@Override
//			public void handleException(StompSession session, StompCommand command, StompHeaders headers,
//					byte[] payload, Throwable exception) {
//				System.out.println("excepción1 " + exception + "    " + new String(payload));
//				
//			}
//			@Override
//			public void handleTransportError(StompSession session, Throwable exception) {
//				System.out.println("excepción2 " + exception);
//			}
//		};
//		StompSession sesion = stompClient.connect("ws://localhost/gs-guide-websocket", sessionHandler).get();
//		
//		while(!sesion.isConnected()) {
//			synchronized (LOCK) {
//				LOCK.wait();
//			}
//		}
//
//	    Subscription s = sesion.subscribe("/topic/greetings", sessionHandler);
//	    sesion.send("/app/hello", new Empleado("a","b",555));
//	    System.out.println("mensaje enviado");
//	    
//		new Scanner(System.in).nextLine(); // Don't close immediately.
//		
//		sesion.disconnect();
		
		
		
		
	
	}

//	@Bean
//	public static RestTemplate restTemplate() {
//		var factory = new SimpleClientHttpRequestFactory();
//		factory.setConnectTimeout(3000);
//		factory.setReadTimeout(3000);
//		return new RestTemplate(factory);
//	}


}
