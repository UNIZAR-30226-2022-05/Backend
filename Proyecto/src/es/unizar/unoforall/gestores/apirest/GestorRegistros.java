package es.unizar.unoforall.gestores.apirest;

import java.util.HashMap;
import java.util.Map;


import javax.swing.Timer;

import es.unizar.unoforall.db.UsuarioDAO;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.utils.Mail;



public class GestorRegistros {
	//El registro temporal exipirará en 5 min
	private final static int EXPIRACION_REGISTRO = 5*60000;  
	
	private final static int MAX_CODIGO = 999999;
	private final static int MIN_CODIGO = 100000;
	
	public static Map<String,RegistroTemporal> usuariosPendientes;
	
	private static final Object LOCK;
	
	static {
		usuariosPendientes = new HashMap<>();
		LOCK = new Object();
	}
	
	/**
	 * Añade al Map el usuario <<user>> si no hay uno que ya tenga el mismo correo
	 * @param user 	Contiene el correo, nombre y contraseña (hash) a introducir 
	 * 				en la base de datos.
	 * @return 		"nulo" si no se ha producido ningún error, y el motivo del 
	 * 				error en caso contrario
	 */
	public static String anadirUsuario(UsuarioVO user) {
		synchronized (LOCK) {
			String error = null;
			if (!usuariosPendientes.containsKey(user.getCorreo()) &&
					!GestorActualizaCuentas.contieneCorreo(user.getCorreo())) {
				int codigo = (int) ((Math.random() * (MAX_CODIGO - MIN_CODIGO)) + MIN_CODIGO);
				
				
				boolean exitoMail = Mail.sendMail(user.getCorreo(), 
					"Verificación de la cuenta en UNOForAll", 
					"Su código de verificación es: " + Integer.toString(codigo) +
					".\nRecuerde que si tarda más de 5 minutos tendrá que volver a "
					+ "registrarse (podrá usar el mismo correo)");
				
				if (!exitoMail)	{
					System.err.println("Error al enviar el mail");
					error = "Fallo en el servidor: no se pudo enviar el correo";
				} else {
					AlarmaRegistro alarm = new AlarmaRegistro(user.getCorreo());
					Timer t = new Timer(EXPIRACION_REGISTRO,alarm);
					RegistroTemporal rt = new RegistroTemporal(user,t,codigo);
					usuariosPendientes.put(user.getCorreo(),rt);
					t.start();
				}
			} else {
				error = "El correo ya está vinculado a una petición de registro o de actualización de cuenta.";
			}
			return error;
		}
	}
	
	
	/**
	 * Verifica que el código es el que está asociado al correo del map
	 * @param correo	correo del usuario
	 * @param codigo	código recibido
	 * @return			"nulo" si no se ha producido ningún error, y el motivo del 
	 * 					error en caso contrario
	 */
	public static String confirmarRegistro(String correo, Integer codigo) {
		synchronized (LOCK) {
			String error = null;
			if (usuariosPendientes.containsKey(correo)) {
				if (usuariosPendientes.get(correo).getCodigo()==codigo) {
					UsuarioDAO.registrarUsuario(usuariosPendientes.get(correo).getUsuario());
					usuariosPendientes.get(correo).getTimer().stop();
					usuariosPendientes.remove(correo);
				} else {
					error = "Código incorrecto";
				}
			} else {
				error = "Su petición de registro ha expirado, vuelva a realizarla.";
			}
			
			return error;
		}
	}
	
	/**
	 * Verifica que el código es el que está asociado al correo del map
	 * @param correo	correo del usuario
	 * @param codigo	código recibido
	 * @return			"nulo" si no se ha producido ningún error, y el motivo del 
	 * 					error en caso contrario
	 */
	public static String cancelarRegistro(String correo) {
		synchronized (LOCK) {
			String error = null;
			if (usuariosPendientes.containsKey(correo)) {
				GestorRegistros.usuariosPendientes.get(correo).getTimer().stop();
				usuariosPendientes.remove(correo);
			} else {
				error = "No hay una petición de registro con este correo. Puede que ya esté registrado o que su petición haya expirado.";
			}
			return error;
		}
	}
}
