package es.unizar.unoforall.apirest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.unizar.unoforall.db.PoolConnectionManager2;
import es.unizar.unoforall.model.*;
import es.unizar.unoforall.utils.Mail;


//Indiciamos que es un controlador rest
@RestController
@RequestMapping("/api")
public class ApiRestController {
	 
	
	@PostMapping("/login")
	public LoginResponse logUser(@RequestParam String correo, @RequestParam String contrasenna){
		UsuarioVO usuario = UsuarioDAO.getUsuario(correo);
		
		if (usuario == null) {
			return new LoginResponse(false, "Usuario no registrado", null);
		} else if (!usuario.getContrasenna().equals(contrasenna))  {
			return new LoginResponse(false, "Contraseña incorrecta", null);
		} else {
			UUID sessionID = GestorSesiones.nuevaSesion(usuario);	
			return new LoginResponse(true, "", sessionID);
		}
    }
	
	
//	@PostMapping("/register")
//	public LoginResponse registerUser(@RequestParam UsuarioVO usuario){
//		UsuarioDAO.registrarUsuario(usuario);
//    }
//	
	
    /*Este método se hará cuando por una petición GET (como indica la anotación) se llame a la url 
    http://127.0.0.1:8080/api/users*/
	@GetMapping("/users")
	public String getUser(@RequestParam String userId){
        //retornará todos los usuarios
        return "has escrito " + userId;
    }
	
	@GetMapping("/usuario")
	public UsuarioVO getEmpleados(@RequestParam String correo){
		Mail.sendMail("801397@unizar.es", "pruebita", "adiós españa\n holi");
		return UsuarioDAO.getUsuario(correo);
		
    }
	
	//PARA PRODUCCIÓN
	@GetMapping("/close")
	public String closeConnections(@RequestParam String clave){
		if (clave.equals("unoforall")) {
			PoolConnectionManager2.close();
			return "OK, reinicia el servidor";
		} else {
			return "Contraseña incorrecta";
		}
    }

    /*Este método se hará cuando por una petición GET (como indica la anotación) se llame a la url + el id de un usuario
    http://127.0.0.1:8080/api/users/1*/
//    @GetMapping("/users/{userId}")
//    public User getUser(@PathVariable int userId){
//        User user = userService.findById(userId);
//
//        if(user == null) {
//            throw new RuntimeException("User id not found -"+userId);
//        }
//        //retornará al usuario con id pasado en la url
//        return user;
//    }
	
	//http://127.0.0.1/api/login  
	@PostMapping("/registerStepOne")
	public String getUser(@RequestParam String correo, @RequestParam String contrasenna, @RequestParam String nombre){
        //retornará todos los usuarios
		UsuarioVO user = UsuarioDAO.getUsuario(correo);
		String error = null;
		if (user==null) {
			user = new UsuarioVO(correo,nombre,contrasenna);
			Integer codigo = GestorRegistros.anadirUsuario(user);
			if(codigo!=null) {
				Mail.sendMail(user.getCorreo(), "Verificación de la cuenta en UNOForAll", "Su código de verificación es: "+Integer.toString(codigo)+".\nRecuerde que si tarda más de 5 minutos tendrá que volver a registrarse (podrá usar el mismo correo)");
			} else {
				error = "El correo ya está vinculado a una petición de registro.";
			}
		} else {
			error = "El correo ya está asociado a una cuenta.";
		}
			
        return error;
    }

	
	//http://127.0.0.1/api/login  
	@PostMapping("/registerStepTwo")
	public String getUser(@RequestParam String correo, @RequestParam Integer codigo){
        //retornará todos los usuarios
		String error = GestorRegistros.confirmarRegistro(correo,codigo);	
        return error;
    }
}