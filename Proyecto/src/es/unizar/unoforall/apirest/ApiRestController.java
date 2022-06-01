package es.unizar.unoforall.apirest;


import java.util.UUID;

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
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.sockets.SocketController;
import me.i2000c.web_utils.annotations.PostMapping;
import me.i2000c.web_utils.annotations.RestController;


/**
 * Clase que implementa el API REST
 * @author unoforall
 *
 */
@RestController("/api")
public class ApiRestController{
    
        private final SocketController socketController;
    
        public ApiRestController(SocketController socketController){
            this.socketController = socketController;
        }
	
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
	public RespuestaLogin login(UUID sessionID, String correo, String contrasenna){
		
		UsuarioVO usuario = UsuarioDAO.getUsuario(correo);
		
		if (usuario == null) {
                        socketController.disconnectClient(sessionID);
			return new RespuestaLogin(false, "Usuario no registrado", null, null);
		} else if (!usuario.getContrasenna().equals(contrasenna))  {
                        socketController.disconnectClient(sessionID);
			return new RespuestaLogin(false, "Contraseña incorrecta", null, null);
		} else {
                    if(GestorSesiones.estaLogueado(usuario.getId())){
                        socketController.disconnectClient(sessionID);
                        return new RespuestaLogin(false, "El usuario ya tiene una sesión iniciada", null, null);
                    }else{
                        System.out.println("Nueva sesión: " + sessionID);
                        GestorSesiones.loguearUsuario(sessionID, usuario.getId());
                        return new RespuestaLogin(true, "", null, usuario.getId());
                    }
			/*UUID claveInicio = GestorSesiones.nuevaClaveInicio(usuario.getId());
			if (claveInicio == null) {
				return new RespuestaLogin(false, "El usuario ya tiene una sesión iniciada, o ya está iniciando una", null, null);
			} else {
				return new RespuestaLogin(true, "", claveInicio, usuario.getId());
			}*/
			
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
	public String registerStepOne(String correo, 
				String contrasenna, String nombre){
		String error = null;
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
	public String registerStepTwo(String correo, Integer codigo){
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
	public Boolean registerCancel(String correo){
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
	public String reestablecercontrasennaStepOne(String correo){
		String error = null;
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
	public String reestablecerContrasennaStepTwo(String correo,
												 Integer codigo){		
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
	public String reestablecercontrasennaStepThree(String correo,
												 String contrasenna){		
		UsuarioVO user = UsuarioDAO.getUsuario(correo);
		String error = null;
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
	public UsuarioVO sacarUsuarioVO(UUID sessionID){
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		
		if (usuarioID == null) {
			return new UsuarioVO();
		} else {
			return UsuarioDAO.getUsuario(usuarioID);
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
	public ListaPartidas sacarPartidasJugadas(UUID sessionID,
										UUID usuarioID){
		ListaPartidas lp = null;
		UUID usuarioEmisorID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioEmisorID != null) {
			lp = PartidasDAO.getPartidas(usuarioID);
		} else {
			lp = new ListaPartidas(true);
			lp.setError("SESION_EXPIRADA");
		}
		return lp;
	}
		
	
	/**
	 * Función a la que llamar para borrar la cuenta del usuario activo.
	 * @param sesionID		id de la sesión del usuario
	 * @return				"BORRADA" si ha tenido éxito.
	 * 						"SESION_EXPIRADA" si la sesión del usuario ha expirado.
	 * 						Un mensaje de error si no ha tenido éxito.
	 */
	@PostMapping("/borrarCuenta")
	public String borrarCuenta(UUID sessionID) {
		String resultado = "BORRADA";
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			boolean exito = UsuarioDAO.eliminarUsuario(new UsuarioVO(usuarioID,null,null,null,0,0,0,0,0,0));
			if (!exito) {
				resultado = "Ha surgido un problema al intentar borrar la cuenta.";
			} else {
				GestorSesiones.eliminarSesion(sessionID);
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
	public String actualizarCuentaStepOne(UUID sessionID,
			String correoNuevo, String nombre, 
			String contrasenna){
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
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
	public String actualizarCuentaStepTwo(UUID sessionID,
												 Integer codigo){		
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
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
	public String actualizarCancel(UUID sessionID){
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
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
	public String cambiarPersonalizacion(UUID sessionID,
											Integer avatar,
											Integer aspectoCartas,
											Integer aspectoFondo){
		
		
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
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
	public ListaUsuarios sacarPeticionesEnviadas(UUID sessionID) {
		ListaUsuarios lu = null;
		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarPeticionesEnviadas(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
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
	public ListaUsuarios sacarPeticionesRecibidas(UUID sessionID) {
		ListaUsuarios lu = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarPeticionesRecibidas(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
	}
	
	
	/**
	 * Método al que llamar para sacar los amigos que tiene el usuario.
	 * @param idSesion 	contiene el id de la sesion del usuario;
	 * @return			(clase ListaUsuarios) una lista de usuarios que indica si la sesión ha expirado,
	 * 					si ha habido un error y la lista de usuarios que haya podido
	 * 					extraer.
	 */
	@PostMapping("/sacarAmigos")
	public ListaUsuarios sacarAmigos(UUID sessionID) {
		ListaUsuarios lu = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			lu = UsuarioDAO.sacarAmigos(usuarioID);		
		} else {
			lu = new ListaUsuarios(true);
		}
		return lu;
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
	public String aceptarPeticionAmistad(UUID sessionID,
								UUID amigo) {
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			error = UsuarioDAO.mandarPeticion(usuarioID, amigo); //Acepta porque ya existe la petición.		
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
	public String cancelarPeticionAmistad(UUID sessionID,
								UUID amigo) {
		String error = null;
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			error = UsuarioDAO.cancelarPeticion(usuarioID, amigo); 
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
	public ListaUsuarios buscarAmigo(UUID sessionID, 
							String amigo) {
			ListaUsuarios usuario = null;
			UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
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
			return usuario;
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
	public RespuestaSala crearSala(UUID sessionID, ConfigSala configuracion){
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			return new RespuestaSala(true, "", GestorSalas.nuevaSala(configuracion));
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
	public Sala buscarSalaID(UUID sessionID, String salaID){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		UUID _salaID;
		try {
			_salaID = UUID.fromString(salaID);
		} catch (IllegalArgumentException e) {
			System.out.println("Ha llegado un UUID inválido");
			return new Sala("No se ha encontrado la sala");
		}
		if(usuarioID != null) {
			Sala salaADevolver = GestorSalas.buscarSalaID(_salaID);
			if (salaADevolver == null) {
				return new Sala("No se ha encontrado la sala");
			} else if (!salaADevolver.puedeUnirse()){
				return new Sala("La sala no es pública");
			}else{
				return salaADevolver;
			}
		} else {
			return new Sala("Sesión expirada");
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
	public RespuestaSalas filtrarSalas(UUID sessionID, 
							ConfigSala configuracion){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			return new RespuestaSalas(GestorSalas.buscarSalas(configuracion)).getRespuestaAEnviar();
		} else {
			return new RespuestaSalas();
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
	public Sala comprobarPartidaPausada(UUID sessionID){		
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if(usuarioID != null) {
			Sala salaPausada = GestorSalas.getSalaPausada(usuarioID);
			if (salaPausada != null) {
				return salaPausada.getSalaAEnviar(); 
			} else {
				return new Sala("No hay ninguna sala pausada").getSalaAEnviar();
			}

		} else {
			return new Sala("No hay ninguna sala pausada").getSalaAEnviar();
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
	public boolean comprobarUnirseSala(UUID sessionID, UUID salaID){
		Sala sala = GestorSalas.obtenerSala(salaID);
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (usuarioID == null 
				|| sala == null
				|| sala.isEnPartida() 
				|| (sala.isEnPausa() && !sala.hayParticipante(usuarioID))) {
			return false;
		} else {
			return true;
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
	public boolean ack(UUID sessionID, UUID salaID){
            if(0==0)return true;
            
		System.out.println("ACK recibido");
		Sala sala = GestorSalas.obtenerSala(salaID);
		UUID usuarioID = GestorSesiones.obtenerUsuarioID(sessionID);
		if (sala != null && usuarioID != null) {
			sala.ack(usuarioID);
			return true;
		} else {
			return false;
		}
    }
	
	
	/**
	 * SOLO PRODUCCIÓN
	 * Método para cerrar las conexiones de la BD
	 * @param clave
	 * @return
	 */
	//@GetMapping("/close")
	public String closeConnections(String clave){
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