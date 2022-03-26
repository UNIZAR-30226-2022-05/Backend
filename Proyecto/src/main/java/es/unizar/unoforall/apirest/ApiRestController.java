package es.unizar.unoforall.apirest;


import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.salas.GestorSalas;
import es.unizar.unoforall.sesiones.GestorSesiones;
import es.unizar.unoforall.sesiones.Sesion;
import es.unizar.unoforall.utils.CaracteresInvalidos;
import es.unizar.unoforall.utils.Deserializar;


/**
 * Clase que implementa el API REST
 * @author unoforall
 *
 */
@RestController
@RequestMapping("/api")
public class ApiRestController {
	
	/**
	 * Método para loguear un usuario
	 * @param correo		correo del usuario
	 * @param contrasenna	hash de la contraseña del usuario
	 * @return 				RespuestaLogin.isExito = true si no ha habido errores
	 * 							RespuestaLogin.sesionID tiene el id de sesión
	 * 						RespuestaLogin.isExito = false en caso contrario
	 * 							RespuestaLogin.errorInfo especifica el motivo del error
	 */
	@PostMapping("/login")
	public RespuestaLogin login(@RequestParam String correo, 
									@RequestParam String contrasenna){
		
		UsuarioVO usuario = UsuarioDAO.getUsuario(correo);
		
		if (usuario == null) {
			return new RespuestaLogin(false, "Usuario no registrado", null);
		} else if (!usuario.getContrasenna().equals(contrasenna))  {
			return new RespuestaLogin(false, "Contraseña incorrecta", null);
		} else {
			UUID sesionID = GestorSesiones.nuevaSesion(usuario);	
			return new RespuestaLogin(true, "", sesionID);
		}
    }
	
	
	/**
	 * Método para cerrar la sesión por parte del usuario
	 * @param 	sessionID contiene el id de la sesión
	 * @return 	true en caso de que se haya cerrado la sesión.
	 * 		   	false en caso de que no exista dicha sesión.
	 */
	@PostMapping("/cerrarSesion")
	public Boolean cerrarSesion(@RequestParam UUID sesionID) {
		boolean exito = false;
		Sesion s = GestorSesiones.obtenerSesion(sesionID);
		if (s!=null) {
			exito = true;
			GestorSesiones.eliminarSesion(sesionID);
		}
		return exito;
	}
	
	
	/**
	 * Método para comprobar si el id de sesión es válido todavía
	 * @param 	sessionID contiene el id de la sesión
	 * @return 	true en caso de que la sesión siga activa.
	 * 		   	false en caso de que no exista dicha sesión.
	 */
	@PostMapping("/comprobarSesion")
	public Boolean comprobarSesion(@RequestParam UUID sesionID) {
		Sesion s = GestorSesiones.obtenerSesion(sesionID);
		if (s!=null) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	/**
	 * Método para registrar un usuario - PASO 1: rellenar información
	 * @param correo		correo del usuario
	 * @param contrasenna	hash de la contraseña del usuario
	 * @param nombre		nombre del usuario
	 * @return				null si no ha habido ningún error
	 * 						mensaje de error si se ha producido alguno
	 */
	@PostMapping("/registerStepOne")
	public String registerStepOne(@RequestParam String correo, 
				@RequestParam String contrasenna, @RequestParam String nombre){
		String error = null;
		if (!CaracteresInvalidos.hayCaracteresInvalidos(correo)
				&& !CaracteresInvalidos.hayCaracteresInvalidos(contrasenna)
				&& !CaracteresInvalidos.hayCaracteresInvalidos(nombre)) {
			UsuarioVO user = UsuarioDAO.getUsuario(correo);
			if (user==null) {
					user = new UsuarioVO(correo,nombre,contrasenna);
					error = GestorRegistros.anadirUsuario(user);
			} else {
				error = "El correo ya está asociado a una cuenta.";
			}
		} else {
			error = "Los campos introducidos contienen caracteres inválidos.";
		}	
        return error;
    }
	
	
	/**
	 * Método para registrar un usuario - PASO 2: confirmar el correo 
	 * introduciendo el código enviado a este
	 * @param correo	correo del usuario
	 * @param codigo	código enviado al correo
	 * @return			null si no ha habido ningún error
	 * 					mensaje de error si se ha producido alguno
	 */
	@PostMapping("/registerStepTwo")
	public String registerStepTwo(@RequestParam String correo, @RequestParam Integer codigo){
		String error = GestorRegistros.confirmarRegistro(correo,codigo);	
        return error;
    }
	
	/**
	 * Función a la que llamar cuando se cancela un registro. Para evitar boicoteos mejor que
	 * solo se pueda llamar desde la ventana de confirmación de código.
	 * @param correo 	contiene el correo con el que se ha inciado la solicitud de registro.
	 * @return 			true en caso de que se haya podido cancelar la petición de registro.
	 * 		   			false en caso contrario (probablemente debido a que ya ha expirado).
	 */
	@PostMapping("/registerCancel")
	public Boolean registerCancel(@RequestParam String correo){
		boolean exito = false;
        if (GestorRegistros.usuariosPendientes.containsKey(correo)) {
        	exito = true;
        	GestorRegistros.cancelarRegistro(correo);
        }
		return exito;
    }
	
	/**
	 * Función a la que llamar para solicitar cambiar algún dato de la cuenta. Manda
	 * un código al correo, con el que se pasa al paso dos.
	 * @param idSesion 		id de la sesión del usuario.
	 * @param correoNuevo 	nuevo valor de correo para la cuenta, puede ser el mismo
	 * 		 			  	que correoViejo.
	 * @param nombre 		nuevo nombre para la cuenta
	 * @param contrasenya 	nuevo valor para la contraseña de la cuenta. 
	 * @return 				un String con un mensaje de error que es null si todo va bien.
	 * 						En caso de que no haya una sesión asignada a al id dado, devuelve "SESION_EXPIRADA".
	 * 		   				En cualquier otro caso, la información estará contenida en el String.
	 */
	@PostMapping("/actualizarCuentaStepOne")
	public String actualizarCuentaStepOne(@RequestParam UUID idSesion,
								String correoNuevo, String nombre, String contrasenya){
		String error = null;
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			if (!CaracteresInvalidos.hayCaracteresInvalidos(correoNuevo) &&
						!CaracteresInvalidos.hayCaracteresInvalidos(nombre) &&
						!CaracteresInvalidos.hayCaracteresInvalidos(contrasenya)) { 
				
				UsuarioVO user = UsuarioDAO.getUsuario(sesion.getMiUsuario().getCorreo());
				if (user!=null) {
					GestorActualizaCuentas.anyadirPeticion(user.getCorreo(), correoNuevo,
																contrasenya, nombre);
				} else {
					error = "La cuenta ya no existe.";
				}
			} else {
				error = "Los campos introducidos contienen caracteres inválidos.";
			}
		} else {
			error = "SESION_EXPIRADA";
		}
			
        return error;
    }
	
	/**
	 * Función a la que llamar para confirmar el código y aplicar el cambio. 
	 * @param idSesion	id de la sesión del usuario.
	 * @param codigo	código enviado al correo pasado por parámetro.
	 * @return			null si no ha habido ningún error.
	 * 					En caso de que no haya una sesión asignada a al id dado, devuelve "SESION_EXPIRADA". 		   				
	 * 					Devuelve un mensaje con información del error si se ha producido alguno.
	 */
	@PostMapping("/actualizarCuentaStepTwo")
	public String actualizarCuentaStepTwo(@RequestParam UUID idSesion,
												 @RequestParam Integer codigo){		
		String error = null;
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			error = GestorActualizaCuentas.confirmarCodigo(sesion.getMiUsuario().getCorreo(), codigo);
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
    }
	
	/**
	 * Función a la que llamar cuando se cancela una actualización. Para evitar boicoteos mejor que
	 * solo se pueda llamar desde la ventana de confirmación de código.
	 * @param correo 	contiene el nuevo correo que se había planteado para la actualización.
	 * @return 			null en caso de que se haya podido cancelar la petición de registro.
	 * 		   			"SESION_EXPIRADA" en caso de que la sesión del usuario haya caducado.
	 * 					un String especificando el error que haya sucedido.
	 */
	@PostMapping("/actualizarCancel")
	public String actualizarCancel(@RequestParam UUID idSesion){
		String error = null;
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			error = GestorActualizaCuentas.cancelarActualizacion(sesion.getMiUsuario().getCorreo());
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
    }
	
	/**
	 * Función a la que llamar para solicitar reestablecer la contraseña. Manda
	 * un código al correo, con el que se pasa al paso dos
	 * @param correo 	correo de la cuenta a cambiar la contraseña
	 * @return 			un String con un mensaje de error que es null si todo va bien.
	 * 		   			Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/reestablecerContrasenyaStepOne")
	public String reestablecerContrasenyaStepOne(@RequestParam String correo){
		String error = null;
		if (!CaracteresInvalidos.hayCaracteresInvalidos(correo)) { //Esto cuando esté definida la clase CaracteresInvalidos
			UsuarioVO user = UsuarioDAO.getUsuario(correo);
			
			if (user!=null) {
				GestorContrasenyas.anyadirPeticion(correo);
			} else {
				error = "El correo no está asociado a ninguna cuenta.";
			}
		} else {
			error = "Los campos introducidos contienen caracteres inválidos.";
		}
			
        return error;
    }
	
	/**
	 * Función a la que llamar para comprobar si el código introducido es correcto.
	 * En caso de serlo, habrá que llamar a la función <reestablecerContrasenyaStepThree> 
	 * acorde a su especificación.
	 * @param correo 	contiene el correo de la cuenta a cambiar la contraseña.
	 * @param codigo 	contiene el código introducido por el usuario.
	 * @return 			un String null si todo va bien.
	 * 		   			Si ocurre algo, la información estará contenida en el String.
	 */
//	@PostMapping("/reestablecerContrasennaStepTwo")
//	public String reestablecerContrasennaStepTwo(@RequestParam String correo,
//												 @RequestParam Integer codigo){		
//		String error = GestorContrasenyas.confirmarCodigo(correo, codigo);
//        return error;
//    }
	
	/**
	 * Función a la que llamar para modificar la contrasenya asociada a la cuenta
	 * especificada por el correo del usuario.
	 * @param correo 		contiene el correo de la cuenta a cambiar la contraseña.
	 * @param contrasenya 	contiene la nueva contrasenya de la cuenta (hash).
	 * @return 				un String null si todo va bien.
	 * 		   				Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/reestablecerContrasenyaStepThree")
	public String reestablecerContrasenyaStepThree(@RequestParam String correo,
												 @RequestParam String contrasenya){		
		UsuarioVO user = UsuarioDAO.getUsuario(correo);
		String error = null;
		if (user!=null) {
			error = UsuarioDAO.cambiarContrasenya(user.getId(), contrasenya);
		} else {
			error = "La cuenta que desea cambiar ya no existe.";
		}
		return error;
    }
	
	/**
	 * Método al que llamar para sacar las solicitudes de amistad que ha hecho el usuario
	 * y que aún no se han aceptado.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarPeticionesEnviadas")
	public ListaUsuarios sacarPeticionesEnviadas(@RequestParam UUID idSesion) {
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		ListaUsuarios lu = null;
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			lu = UsuarioDAO.sacarPeticionesEnviadas(sesion.getMiUsuario().getId());		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
	}
	
	/**
	 * Método al que llamar para sacar las solicitudes de amistad que ha hecho el usuario
	 * y que aún no se han aceptado.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarPeticionesRecibidas")
	public ListaUsuarios sacarPeticionesRecibidas(@RequestParam UUID idSesion) {
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		ListaUsuarios lu = null;
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			//lu = UsuarioDAO.sacarPeticionesRecibidas(sesion.getMiUsuario().getId());		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
	}
	
	/**
	 * Método al que llamar para sacar los amigos que tiene el usuario.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarAmigos")
	public ListaUsuarios sacarAmigos(@RequestParam UUID idSesion) {
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		ListaUsuarios lu = null;
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			//lu = UsuarioDAO.sacarAmigos(sesion.getMiUsuario().getId());		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
	}
	
	/**
	 * Método al que llamar para enviar una solicitud de amistad a otro usuario. Si ya se había recibido una
	 * solicitud de dicho usuario, se toma como si se aceptase.
	 * @param idSesion 	contiene el id de la sesion del usuario.
	 * @param amigo		contiene el id de la cuenta del amigo.
	 * @return			
	 */
	@PostMapping("/mandarPeticionAmistad")
	public ListaUsuarios mandarPeticionAmistad(@RequestParam UUID idSesion, 
															@RequestParam UUID amigo) {
		Sesion sesion = GestorSesiones.obtenerSesion(idSesion);
		ListaUsuarios lu = null;
		if(sesion!=null) {
			sesion.getMiTimer().restart();
			//lu = UsuarioDAO.mandarPeticion(sesion.getMiUsuario().getId());		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
	}
	
	/**************************************************************************/
	// Salas
	/**************************************************************************/
	
	/**
	 * Método para crear una sala con la configuración especificada, y a la que
	 * comenzará a pertenecer el usuario
	 * @param sessionID			id de seisón del usuario
	 * @param configuracion		configuración de la sala
	 * @return					id de la sala creada
	 * 							null si no ha sido posible crear la sala
	 */
	@PostMapping("/crearSala")
	public UUID crearSala(@RequestParam String sesionID, @RequestParam String configuracion){		
		
		UUID _sesionID = Deserializar.deserializar(sesionID, UUID.class);
		ConfigSala _configuracion = Deserializar.deserializar(configuracion, ConfigSala.class);
		
		UUID salaID;
		Sesion s = GestorSesiones.obtenerSesion(_sesionID);
		if (s!=null) {
			salaID = GestorSalas.nuevaSala(_configuracion);
		} else {
			return null;
		}
		return salaID;
    }
	
	/**
	 * Método para buscar una sala pública con la configuración especificada
	 * @param sessionID			id de seisón del usuario
	 * @param salaID			id de la sala
	 * @param configuracion		configuración deseada
	 * @return					null si no ha habido ningún error
	 * 		   					mensaje de error si se ha producido
	 */
	@PostMapping("/buscarSala")
	public String buscarSala(@RequestParam String sessionID, @RequestParam UUID salaID,
							@RequestParam ConfigSala configuracion){		
		//TODO
		return null;
    }
	
	
	

	
	
	
	/**
	 * SOLO PRODUCCIÓN
	 * Método para cerrar las conexiones de la BD
	 * @param clave
	 * @return
	 */
	@GetMapping("/close")
	public String closeConnections(@RequestParam String clave){
		if (clave.equals("unoforall")) {
			GestorPoolConexionesBD.close();
			return "OK, reinicia el servidor";
		} else {
			return "Contraseña incorrecta";
		}
    }

	
}