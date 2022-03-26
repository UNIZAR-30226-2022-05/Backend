package es.unizar.unoforall.apirest;


import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.unizar.unoforall.apirest.partida.GestorSalas;
import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partida.ConfigSala;
import es.unizar.unoforall.utils.CaracteresInvalidos;


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
	public Boolean cerrarSesion(@RequestParam UUID sessionID) {
		boolean exito = false;
		Sesion s = GestorSesiones.obtenerSesion(sessionID);
		if (s!=null) {
			exito = true;
			GestorSesiones.eliminarSesion(sessionID);
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
	public Boolean comprobarSesion(@RequestParam UUID sessionID) {
		Sesion s = GestorSesiones.obtenerSesion(sessionID);
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
	 * @param correoViejo 	correo antiguo de la cuenta.
	 * @param correoNuevo 	nuevo valor de correo para la cuenta, puede ser el mismo
	 * 		 			  	que correoViejo.
	 * @param nombre 		nuevo nombre para la cuenta
	 * @param contrasenya 	nuevo valor para la contraseña de la cuenta. 
	 * @return 				un String con un mensaje de error que es null si todo va bien.
	 * 		   				Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/actualizarCuentaStepOne")
	public String actualizarCuentaStepOne(@RequestParam String correoViejo,
								String correoNuevo, String nombre, String contrasenya){
		String error = null;
		if (!CaracteresInvalidos.hayCaracteresInvalidos(correoViejo) &&
					!CaracteresInvalidos.hayCaracteresInvalidos(correoNuevo) &&
					!CaracteresInvalidos.hayCaracteresInvalidos(nombre) &&
					!CaracteresInvalidos.hayCaracteresInvalidos(contrasenya)) { 
			
			UsuarioVO user = UsuarioDAO.getUsuario(correoViejo);
			if (user!=null) {
				GestorActualizaCuentas.anyadirPeticion(correoViejo, correoNuevo,
															contrasenya, nombre);
			} else {
				error = "La cuenta ya no existe.";
			}
		} else {
			error = "Los campos introducidos contienen caracteres inválidos.";
		}
			
        return error;
    }
	
	/**
	 * Función a la que llamar cuando se cancela una actualización. Para evitar boicoteos mejor que
	 * solo se pueda llamar desde la ventana de confirmación de código.
	 * @param correo 	contiene el nuevo correo que se había planteado para la actualización.
	 * @return 			true en caso de que se haya podido cancelar la petición de registro.
	 * 		   			false en caso contrario (probablemente debido a que ya ha expirado).
	 */
	@PostMapping("/actualizarCancel")
	public Boolean actualizarCancel(@RequestParam String correo){
        String error = GestorActualizaCuentas.cancelarActualizacion(correo);
		return error == null;
    }
	
	
	/**
	 * Función a la que llamar para confirmar el código y aplicar el cambio. 
	 * @param correo	nuevo valor del correo del usuario.
	 * @param codigo	código enviado al correo pasado por parámetro.
	 * @return			null si no ha habido ningún error.
	 * 					mensaje de error si se ha producido alguno.
	 */
	@PostMapping("/actualizarCuentaStepTwo")
	public String actualizarCuentaStepTwo(@RequestParam String correo,
												 @RequestParam Integer codigo){		
		String error = GestorActualizaCuentas.confirmarCodigo(correo, codigo);
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
	 * Método para crear una sala con la configuración especificada, y a la que
	 * comenzará a pertenecer el usuario
	 * @param sessionID			id de seisón del usuario
	 * @param configuracion		configuración de la sala
	 * @return					null si no ha habido ningún error
	 * 		   					mensaje de error si se ha producido
	 */
	@PostMapping("/crearSala")
	public String crearSala(@RequestParam UUID sessionID, @RequestParam ConfigSala configuracion){		
		String error = null;
		Sesion s = GestorSesiones.obtenerSesion(sessionID);
		if (s!=null) {
			GestorSalas.nuevaSala(configuracion, GestorSesiones.obtenerSesion(sessionID).getUsuario());
		} else {
			error = "La sesión ha caducado";
		}
		return error;
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