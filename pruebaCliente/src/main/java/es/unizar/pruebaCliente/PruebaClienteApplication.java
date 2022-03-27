package es.unizar.pruebaCliente;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.reflect.TypeToken;

import es.unizar.pruebaCliente.salas.ConfigSala;
import es.unizar.pruebaCliente.salas.NotificacionSala;
import es.unizar.pruebaCliente.salas.ReglasEspeciales;
import es.unizar.pruebaCliente.salas.Sala;


@SpringBootApplication
public class PruebaClienteApplication {
	
//	static class Empleado {
//		public String nombre;
//		public String apellido;
//		public int sueldo;
//		
//		public Empleado() {}
//		
//		public Empleado(String n, String a, int s) {
//			nombre = n;
//			apellido = a;
//			sueldo = s;
//		}
//	}
	
	private static Object LOCK = new Object();
	private static String sesionID = "EMPTY";
	
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
		RestAPI apirest = new RestAPI("/api/login");
		apirest.addParameter("correo", correo);
		apirest.addParameter("contrasenna", contrasenna);
		apirest.setOnError(e -> {System.out.println(e);});
    	
		apirest.openConnection();
    	RespuestaLogin resp = apirest.receiveObject(RespuestaLogin.class);
		System.out.println("clave inicio: " + resp.getClaveInicio());
		
		if(resp.getClaveInicio() == null) {
			return;
		}
				
		//CONEXION
		WebSocketAPI api = new WebSocketAPI();
    	
    	api.openConnection();
    	
    	api.subscribe("/topic/conectarse/" + resp.getClaveInicio(), String.class, s -> {
    		sesionID = s;
    		System.out.println("ID sesión: " + sesionID);
    		synchronized (LOCK) {
				LOCK.notify();
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
		
    	
    	ReglasEspeciales reglas = new ReglasEspeciales(false, false, false, false, false, false, false);
    	ConfigSala config = new ConfigSala(ConfigSala.ModoJuego.Original, reglas, 4, true);
		
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
					UUID salaID = apirest.receiveObject(UUID.class);
					System.out.println("sala creada:" + salaID);
				
					
				} else if (orden.equals("unirse")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					
			    	api.subscribe("/topic/salas/" + salaID, Sala.class, s -> {
			    		System.out.println("Estado de la sala: " + s);
			    	});
			    	
			    	api.sendObject("/app/salas/unirse/" + salaID, "vacio");
			    	
			    	
				} else if (orden.equals("listo")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					api.sendObject("/app/salas/listo/" + salaID, "vacio");
					
					
				} else if (orden.equals("salir")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					api.sendObject("/app/salas/salir/" + salaID, "vacio");
					api.unsubscribe("/topic/salas/" + salaID);
					
					
				} else if (orden.equals("buscar")) {
					System.out.println("Introduce id sala:");
					String salaID = scanner.nextLine();
					
					apirest = new RestAPI("/api/buscarSalaID");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("salaID", salaID);
					apirest.setOnError(e -> {System.out.println(e);});
			    	
					apirest.openConnection();
					Sala r = apirest.receiveObject(Sala.class);
					System.out.println("sala encontrada:" + r);
				
					
				} else if (orden.equals("filtrar")) {
					apirest = new RestAPI("/api/filtrarSalas");
					apirest.addParameter("sesionID", sesionID);
					apirest.addParameter("configuracion", config);
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