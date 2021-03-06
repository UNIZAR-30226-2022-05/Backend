package es.unizar.unoforall.apirest;


import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.db.PartidasDAO;
import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.gestores.GestorSalas;
import es.unizar.unoforall.gestores.GestorSesiones;
import es.unizar.unoforall.gestores.apirest.GestorActualizaCuentas;
import es.unizar.unoforall.gestores.apirest.GestorContrasennas;
import es.unizar.unoforall.gestores.apirest.GestorRegistros;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.ListaPartidas;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.RespuestaSala;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CaracteresInvalidos;
import es.unizar.unoforall.api.*;
import es.unizar.unoforall.model.ListaUsuarios;


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
	 * 							RespuestaLogin.claveInicio tiene la clave de inicio
	 * 							RespuestaLogin.usuarioID tiene el uuid del usuario
	 * 						RespuestaLogin.isExito = false en caso contrario
	 * 							RespuestaLogin.errorInfo especifica el motivo del error
	 */
	@PostMapping("/login")
	public RespuestaLogin login(@RequestParam String correo, 
									@RequestParam String contrasenna){
		
		UsuarioVO usuario = UsuarioDAO.getUsuario(correo);
		
		if (usuario == null) {
			return new RespuestaLogin(false, "Usuario no registrado", null, null);
		} else if (!usuario.getContrasenna().equals(contrasenna))  {
			return new RespuestaLogin(false, "Contraseña incorrecta", null, null);
		} else {
			UUID claveInicio = GestorSesiones.nuevaClaveInicio(usuario.getId());
			if (claveInicio == null) {
				return new RespuestaLogin(false, "El usuario ya tiene una sesión iniciada, o ya está iniciando una", null, null);
			} else {
				return new RespuestaLogin(true, "", claveInicio, usuario.getId());
			}
			
		}
    }
	
	
	/**
	 * Método para registrar un usuario - PASO 1: rellenar información
	 * @param correo		correo del usuario
	 * @param contrasenna	hash de la contraseña del usuario
	 * @param nombre		nombre del usuario
	 * @return				"nulo" si no ha habido ningún error
	 * 						mensaje de error si se ha producido alguno
	 */
	@PostMapping("/registerStepOne")
	public String registerStepOne(@RequestParam String correo, 
				@RequestParam String contrasenna, @RequestParam String nombre){
		String error = "nulo";
		if (!CaracteresInvalidos.hayCaracteresInvalidos(correo)
				&& !CaracteresInvalidos.hayCaracteresInvalidos(contrasenna)
				&& !CaracteresInvalidos.hayCaracteresInvalidos(nombre)) {
			UsuarioVO user = UsuarioDAO.getUsuario(correo);
			if (user==null) {
					user = new UsuarioVO(null,correo,nombre,contrasenna);
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
	 * Función a la que llamar para solicitar reestablecer la contraseña. Manda
	 * un código al correo, con el que se pasa al paso dos
	 * @param correo 	correo de la cuenta a cambiar la contraseña
	 * @return 			un String con un mensaje de error que es "nulo" si todo va bien.
	 * 		   			Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/reestablecerContrasennaStepOne")
	public String reestablecercontrasennaStepOne(@RequestParam String correo){
		String error = "nulo";
		if (!CaracteresInvalidos.hayCaracteresInvalidos(correo)) { //Esto cuando esté definida la clase CaracteresInvalidos
			UsuarioVO user = UsuarioDAO.getUsuario(correo);
			
			if (user!=null) {
				error = GestorContrasennas.anyadirPeticion(correo);
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
	 * En caso de serlo, habrá que llamar a la función <reestablecercontrasennaStepThree> 
	 * acorde a su especificación.
	 * @param correo 	contiene el correo de la cuenta a cambiar la contraseña.
	 * @param codigo 	contiene el código introducido por el usuario.
	 * @return 			un String null si todo va bien.
	 * 		   			Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/reestablecerContrasennaStepTwo")
	public String reestablecerContrasennaStepTwo(@RequestParam String correo,
												 @RequestParam Integer codigo){		
		String error = GestorContrasennas.confirmarCodigo(correo, codigo);
        return error;
    }
	
	
	/**
	 * Función a la que llamar para modificar la contrasenna asociada a la cuenta
	 * especificada por el correo del usuario.
	 * @param correo 		contiene el correo de la cuenta a cambiar la contraseña.
	 * @param contrasenna 	contiene la nueva contrasenna de la cuenta (hash).
	 * @return 				un String "nulo"si todo va bien.
	 * 		   				Si ocurre algo, la información estará contenida en el String.
	 */
	@PostMapping("/reestablecerContrasennaStepThree")
	public String reestablecercontrasennaStepThree(@RequestParam String correo,
												 @RequestParam String contrasenna){		
		UsuarioVO user = UsuarioDAO.getUsuario(correo);
		String error = "nulo";
		if (user!=null) {
			error = UsuarioDAO.cambiarContrasenna(user.getId(), contrasenna);
		} else {
			error = "La cuenta que desea cambiar ya no existe.";
		}
		return error;
    }
	
	
	
	
	/**************************************************************************/
	// Cuenta
	/**************************************************************************/
	
	/**
	 * Método para obtener el UsuarioVO del usuario
	 * @param sesionID		contiene el id de sesión del usuario.
	 * @return				el UsuarioVO del usuario
	 * 						Si no se ha podido obtener por un fallo de sesión,
	 * 						tendrá el atributo 'exito' a false
	 */
	@PostMapping("/sacarUsuarioVO")
	public String sacarUsuarioVO(@RequestParam String sesionID){
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		
		if (usuarioID == null) {
			return Serializar.serializar(new UsuarioVO());
		} else {
			return Serializar.serializar(UsuarioDAO.getUsuario(usuarioID));
		}
    }
	
	
	/**
	 * Función a la que llamar para sacar el listado de las partidas que ha terminado el
	 * usuario, junto a los datos de cada participante humano que jugó cada una.
	 * @param sesionID		contiene el id de sesión del usuario.
	 * @param usuarioID		id del usuario del que se quiere obtener el historial
	 * @return				(clase ListaPartidas) una lista de partidas y sus participantes que indica si la 
	 * 						sesión ha expirado, si ha habido un error y la lista de 
	 * 						partidas que haya podido extraer.
	 */
	@PostMapping("/sacarPartidasJugadas")
	public String sacarPartidasJugadas(@RequestParam String sesionID,
										@RequestParam String usuarioID){
		ListaPartidas lp = null;
		UUID usuarioEmisorID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioEmisorID != null) {
			UUID _usuarioID = Serializar.deserializar(usuarioID, UUID.class);
			lp = PartidasDAO.getPartidas(_usuarioID);
		} else {
			lp = new ListaPartidas(true);
			lp.setError("SESION_EXPIRADA");
		}
		return Serializar.serializar(lp);
	}
		
	
	/**
	 * Función a la que llamar para borrar la cuenta del usuario activo.
	 * @param sesionID		id de la sesión del usuario
	 * @return				"BORRADA" si ha tenido éxito.
	 * 						"SESION_EXPIRADA" si la sesión del usuario ha expirado.
	 * 						Un mensaje de error si no ha tenido éxito.
	 */
	@PostMapping("/borrarCuenta")
	public String borrarCuenta(@RequestParam String sesionID) {
		String resultado = "BORRADA";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			boolean exito = UsuarioDAO.eliminarUsuario(new UsuarioVO(usuarioID,null,null,null,0,0,0,0,0,0));
			if (!exito) {
				resultado = "Ha surgido un problema al intentar borrar la cuenta.";
			} else {
				GestorSesiones.eliminarSesion(sesionID);
			}
		} else {
			resultado = "SESION_EXPIRADA";
		}
		return resultado;
	}
	
	
	/**
	 * Función a la que llamar para solicitar cambiar algún dato de la cuenta. Manda
	 * un código al correo, con el que se pasa al paso dos.
	 * @param idSesion 		id de la sesión del usuario.
	 * @param correoNuevo 	nuevo valor de correo para la cuenta, puede ser el mismo
	 * 		 			  	que correoViejo.
	 * @param nombre 		nuevo nombre para la cuenta
	 * @param contrasenna 	nuevo valor para la contraseña de la cuenta. 
	 * @return 				un String con un mensaje de error que es "nulo" si todo va bien.
	 * 						En caso de que no haya una sesión asignada a al id dado, devuelve "SESION_EXPIRADA".
	 * 		   				En cualquier otro caso, la información estará contenida en el String.
	 */
	@PostMapping("/actualizarCuentaStepOne")
	public String actualizarCuentaStepOne(@RequestParam String sesionID,
			@RequestParam String correoNuevo, @RequestParam String nombre, 
			@RequestParam String contrasenna){
		String error = "nulo";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			if (!CaracteresInvalidos.hayCaracteresInvalidos(correoNuevo) &&
						!CaracteresInvalidos.hayCaracteresInvalidos(nombre) &&
						!CaracteresInvalidos.hayCaracteresInvalidos(contrasenna)) { 
				
				UsuarioVO user = UsuarioDAO.getUsuario(usuarioID);
				UsuarioVO user2 = UsuarioDAO.getUsuario(correoNuevo);
				if (user!=null) {
					if(correoNuevo.equals(user.getCorreo()) || user2==null){
						error = GestorActualizaCuentas.anyadirPeticion(usuarioID, correoNuevo,
								contrasenna, nombre);
					} else {
						error = "El correo que desea utilizar ya está en uso.";
					}
					
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
	 * @param sesionID	id de la sesión del usuario.
	 * @param codigo	código enviado al correo pasado por parámetro.
	 * @return			"nulo" si no ha habido ningún error.
	 * 					En caso de que no haya una sesión asignada a al id dado, devuelve "SESION_EXPIRADA". 		   				
	 * 					Devuelve un mensaje con información del error si se ha producido alguno.
	 */
	@PostMapping("/actualizarCuentaStepTwo")
	public String actualizarCuentaStepTwo(@RequestParam String sesionID,
												 @RequestParam Integer codigo){		
		String error = "nulo";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			error = GestorActualizaCuentas.confirmarCodigo(usuarioID, codigo);
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
    }
	
	
	/**
	 * Función a la que llamar cuando se cancela una actualización. Para evitar boicoteos mejor que
	 * solo se pueda llamar desde la ventana de confirmación de código.
	 * @param correo 	contiene el nuevo correo que se había planteado para la actualización.
	 * @return 			"nulo" en caso de que se haya podido cancelar la petición de registro.
	 * 		   			"SESION_EXPIRADA" en caso de que la sesión del usuario haya caducado.
	 * 					un String especificando el error que haya sucedido.
	 */
	@PostMapping("/actualizarCancel")
	public String actualizarCancel(@RequestParam String sesionID){
		String error = "nulo";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			error = GestorActualizaCuentas.cancelarActualizacion(usuarioID);
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
    }
	
	
	/**
	 * Función a la que llamar para cambiar la personalización del usuario
	 * @param sesionID	id de la sesión del usuario.
	 * @param avatar 		(0-6) es el nuevo avatar
	 * @param aspectoCartas (0-1) es el nuevo aspecto de las cartas
	 * @param avatar 		(0-2) es el nuevo aspecto del fondo
	 * @return 			"nulo" en caso de que se haya podido cancelar la petición de registro.
	 * 		   			"SESION_EXPIRADA" en caso de que la sesión del usuario haya caducado.
	 * 					un String especificando el error que haya sucedido.
	 */
	@PostMapping("/cambiarAvatar")
	public String cambiarPersonalizacion(@RequestParam String sesionID,
											@RequestParam Integer avatar,
											@RequestParam Integer aspectoCartas,
											@RequestParam Integer aspectoFondo){
		
		
		String error = "nulo";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			error = UsuarioDAO.cambiarAvatar(usuarioID, avatar, aspectoCartas, aspectoFondo);
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
    }
	

	
	
	
	/**************************************************************************/
	// Amigos
	/**************************************************************************/
	
	/**
	 * Método al que llamar para sacar las solicitudes de amistad que ha hecho el usuario
	 * y que aún no se han aceptado.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			(clase ListaUsuarios) una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarPeticionesEnviadas")
	public String sacarPeticionesEnviadas(@RequestParam String sesionID) {
		ListaUsuarios lu = null;
		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarPeticionesEnviadas(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return Serializar.serializar(lu);
	}
	
	
	/**
	 * Método al que llamar para sacar las solicitudes de amistad que ha hecho el usuario
	 * y que aún no se han aceptado.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			(clase ListaUsuarios) una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarPeticionesRecibidas")
	public String sacarPeticionesRecibidas(@RequestParam String sesionID) {
		ListaUsuarios lu = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarPeticionesRecibidas(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return Serializar.serializar(lu);
	}
	
	
	/**
	 * Método al que llamar para sacar los amigos que tiene el usuario.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			(clase ListaUsuarios) una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarAmigos")
	public String sacarAmigos(@RequestParam String sesionID) {
		ListaUsuarios lu = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarAmigos(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return Serializar.serializar(lu);
	}
	
	
	/**
	 * Método al que llamar para aceptar la solicitud de amistad a otro usuario.
	 * @param idSesion 	contiene el id de la sesion del usuario.
	 * @param amigo		contiene el id de la cuenta del amigo.
	 * @return			Devuelve "nulo" si se ha enviado la peticion correctamente.
	 * 					Devuelve "ACEPTADA" si había una petición pendiente y la acepta.
	 * 					Devuelve "SESION_EXPIRADA" si la sesión ha expirado.
	 * 					Devuelve un mensaje de error en otro caso.	 
	 */
	@PostMapping("/aceptarPeticionAmistad")
	public String aceptarPeticionAmistad(@RequestParam String sesionID,
															 @RequestParam String amigo) {
		String error = "nulo";
		UUID _amigo = Serializar.deserializar(amigo, UUID.class);
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			error = UsuarioDAO.mandarPeticion(usuarioID,_amigo); //Acepta porque ya existe la petición.		
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
	}
	
	
	/**
	 * Método al que llamar para cancelar la solicitud de amistad a otro usuario.
	 * @param idSesion 	contiene el id de la sesion del usuario.
	 * @param amigo		contiene el id de la cuenta del amigo.
	 * @return			Devuelve "nulo" si todo ha ido bien.
	 * 					Devuelve "SESION_EXPIRADA" si la sesión ha expirado.
	 * 					Devuelve un mensaje de error en otro caso.	 
	 */
	@PostMapping("/cancelarPeticionAmistad")
	public String cancelarPeticionAmistad(@RequestParam String sesionID,
															 @RequestParam String amigo) {
		String error = "nulo";
		UUID _amigo = Serializar.deserializar(amigo, UUID.class);
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			error = UsuarioDAO.cancelarPeticion(usuarioID,_amigo); 
		} else {
			error = "SESION_EXPIRADA";
		}
		return error;
	}
	
	
	/**
	 * Método al que llamar para buscar a un amigo por correo, (no tienen por qué estar 
	 * registrados como amigos, es para el punto de buscar usuario por correo).
	 * @param idSesion 	contiene el id de la sesion del usuario.
	 * @param amigo		contiene el correo de la cuenta del amigo.
	 * @return			(clase ListaUsuarios) Devuelve un objeto ListaUsuarios que indica si la sesión ha expirado, 
	 * 					informa si ha habido algún error, y el usuario si lo ha encontrado (sin
	 * 					su contraseña.
	 */
	@PostMapping("/buscarAmigo")
	public String buscarAmigo(@RequestParam String sesionID, 
														@RequestParam String amigo) {
			ListaUsuarios usuario = null;
			UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
			if(usuarioID != null) {
				UsuarioVO user = UsuarioDAO.getUsuario(amigo);
				usuario = new ListaUsuarios(false);
				if (user!= null) {
					user.setContrasenna(null);
					usuario.getUsuarios().add(user);
				} else {
					usuario.setError("No se ha podido extraer la cuenta con ese correo");
				}
			} else {
				usuario = new ListaUsuarios(true);
			}
			return Serializar.serializar(usuario);
	}
	
	
	
	
	
	
	/**************************************************************************/
	// Salas
	/**************************************************************************/
	
	/**
	 * Método para crear una sala con la configuración especificada, y a la que
	 * comenzará a pertenecer el usuario
	 * @param sesionID			id de seisón del usuario
	 * @param configuracion		(clase ConfigSala) configuración de la sala
	 * @return					id de la sala creada
	 * 							null si no ha sido posible crear la sala
	 */
	@PostMapping("/crearSala")
	public RespuestaSala crearSala(@RequestParam String sesionID, @RequestParam String configuracion){		
		
		ConfigSala _configuracion = Serializar.deserializar(configuracion, ConfigSala.class);
		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			return new RespuestaSala(true, "", GestorSalas.nuevaSala(_configuracion));
		} else {
			return new RespuestaSala(false, "La sesión ha caducado", null);
		}
	}
	
	/**
	 * Método para buscar una sala pública mediante su id.
	 * Solo se utilizará para previsualizar la configuración de la sala antes de 
	 * unirse, pues para ello solo es necesario el salaID
	 * @param sesionID			id de seisón del usuario
	 * @param salaID			(clase UUID) id de la sala
	 * 
	 * @return					(clase Sala) Sala buscada
	 * 							Su atributo 'noExiste' estará a true si no se ha
	 * 							encontrado ninguna.
	 */
	@PostMapping("/buscarSalaID")
	public String buscarSalaID(@RequestParam String sesionID, @RequestParam String salaID){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		UUID _salaID = Serializar.deserializar(salaID, UUID.class);
		if(usuarioID != null) {
			return Serializar.serializar(GestorSalas.buscarSalaID(_salaID));
		} else {
			return Serializar.serializar(new Sala("No se ha encontrado la sala"));
		}
    }
	
	/**
	 * Método para buscar salas públicas con una determinada configuración
	 * @param sesionID			Id de seisón del usuario
	 * @param configuracion		(clase ConfigSala) Configuración a buscar
	 * 								modoJuego Undefined si no se quiere especificar
	 * 								maxParticipantes = -1 si no se quieren especificar
	 * 								reglas -> su campo reglasValidas a false si no se 
	 * 										  quieren especificar
	 *							Si configuración es null, devolverá todas las salas
	 *
	 * @return					(RespuestaSalas) Salas públicas con un hueco libre 
	 * 							y que no están en partida que cumplen la configuración.
	 * 							El atributo 'exito' de RespuestaSalas estará a 
	 * 							false si ha habido algún problema con la sesión
	 * 							del usuario.
	 */
	@PostMapping("/filtrarSalas")
	public String filtrarSalas(@RequestParam String sesionID, 
											@RequestParam String configuracion){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			ConfigSala _configuracion = Serializar.deserializar(configuracion, ConfigSala.class);
			return Serializar.serializar(new RespuestaSalas(GestorSalas.buscarSalas(_configuracion)).getRespuestaAEnviar());
		} else {
			return Serializar.serializar(new RespuestaSalas());
		}
    }
	
	
	/**
	 * Método para buscar salas públicas con una determinada configuración
	 * @param sesionID			Id de sesión del usuario
	 *
	 * @return					(Sala) Sala pausada si la hay para el usuario de sesionID, o null 
	 * 							si no la hay
	 */
	@PostMapping("/comprobarPartidaPausada")
	public String comprobarPartidaPausada(@RequestParam String sesionID){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if(usuarioID != null) {
			Sala salaPausada = GestorSalas.getSalaPausada(usuarioID);
			if (salaPausada != null) {
				return Serializar.serializar(salaPausada.getSalaAEnviar()); 
			} else {
				return Serializar.serializar(new Sala("No hay ninguna sala pausada").getSalaAEnviar());
			}

		} else {
			return Serializar.serializar(new Sala("No hay ninguna sala pausada").getSalaAEnviar());
		}
    }
	
	
	/**
	 * Método para comprobar si un usuario puede unirse a una sala
	 * @param sesionID			Id de sesión del usuario
	 * @param salaID			Id de la sala
	 * @return					(Boolean) True si el usuario se puede unir a la
	 * 							sala, y false en caso contrario
	 */
	@PostMapping("/comprobarUnirseSala")
	public String comprobarUnirseSala(@RequestParam String sesionID, @RequestParam String salaID){
		Sala sala = GestorSalas.obtenerSala(Serializar.deserializar(salaID, UUID.class));
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (usuarioID == null 
				|| sala == null
				|| sala.isEnPartida() 
				|| (sala.isEnPausa() && !sala.hayParticipante(usuarioID))) {
			return Serializar.serializar(false);
		} else {
			return Serializar.serializar(true);
		}
		
    }
	
	
	/**
	 * Método para comprobar si un usuario puede unirse a una sala
	 * @param sesionID			Id de sesión del usuario
	 * @param salaID			Id de la sala
	 * @return					(Boolean) True si es correcto, false en caso
	 * 							contrario
	 */
	@PostMapping("/ack")
	public String ack(@RequestParam String sesionID, @RequestParam String salaID){
		System.out.println("ACK recibido");
		Sala sala = GestorSalas.obtenerSala(Serializar.deserializar(salaID, UUID.class));
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sesionID);
		if (sala != null && usuarioID != null) {
			sala.ack(usuarioID);
			return Serializar.serializar(true);
		} else {
			return Serializar.serializar(false);
		}
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
	
//	@PostMapping("/insertarPartidaAcabadaTEST")
//	public String insertarPartidaAcabadaTEST(){
//		String error = null;
//		PartidasAcabadasVO pa = new PartidasAcabadasVO(null,new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()+1000000),1,0);
//		ArrayList<HaJugadoVO> participantes = new ArrayList<HaJugadoVO>(); 
//		/*Obsoleto debido a nueva versión de la clase "HaJugadoVO*/
//		//participantes.add(new HaJugadoVO(UUID.fromString("4c2a49ed-48be-4970-9010-edb1faf918f2"),pa.getId(),0,false));
//		//participantes.add(new HaJugadoVO(UUID.fromString("4c2a49ed-48be-4970-9010-edb1faf918f1"),pa.getId(),3,true));
//		//participantes.add(new HaJugadoVO(UUID.fromString("4c2a49ed-48be-4970-9010-edb1faf918f4"),pa.getId(),1,false));
//		
//		
//		//participantes.size()==configuracion.getMaxParticipantes()-numIAs
//		//PartidaJugada pj = new PartidaJugada(pa,participantes);
//		System.out.println("Id Partida: "+pa.getId());
//		//error = PartidasDAO.insertarPartidaAcabada(pj);
//		
//		return Serializar.serializar(error);
//	}

	
}