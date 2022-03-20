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
import es.unizar.unoforall.utils.CaracteresInvalidos;
import es.unizar.unoforall.utils.Mail;


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
	 * @return 				RespuestaLogin.exito = true si no ha habido errores
	 * 							RespuestaLogin.sesionID tiene el id de sesión
	 * 						RespuestaLogin.exito = false en caso contrario
	 * 							RespuestaLogin.errorInfo especifica el motivo del error
	 */
	@PostMapping("/login")
	public RespuestaLogin logUser(@RequestParam String correo, 
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
	 * Método para registrar un usuario
	 * @param correo
	 * @param contrasenna
	 * @param nombre
	 * @return
	 */
	@PostMapping("/registerStepOne")
	public String getUser(@RequestParam String correo, 
				@RequestParam String contrasenna, @RequestParam String nombre){
		
		UsuarioVO user = UsuarioDAO.getUsuario(correo);
		String error = null;
		if (user==null) {
			if (CaracteresInvalidos.comprobarCaracteresString(correo)
				&& CaracteresInvalidos.comprobarCaracteresString(contrasenna)
				&& CaracteresInvalidos.comprobarCaracteresString(nombre)) {
				
				user = new UsuarioVO(correo,nombre,contrasenna);
				error = GestorRegistros.anadirUsuario(user);
			} else {
				error = "Los campos introducidos contienen caracteres inválidos.";
			}
		} else {
			error = "El correo ya está asociado a una cuenta.";
		}
			
        return error;
    }
	
	
	@PostMapping("/registerStepTwo")
	public String getUser(@RequestParam String correo, @RequestParam Integer codigo){
		String error = GestorRegistros.confirmarRegistro(correo,codigo);	
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