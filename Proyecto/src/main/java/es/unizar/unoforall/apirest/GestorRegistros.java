package es.unizar.unoforall.apirest;

import java.util.HashMap;
import java.util.Map;


import javax.swing.Timer;

import es.unizar.unoforall.model.UsuarioVO;



public class GestorRegistros {
	//El registro temporal exipirará en 5 min
	private final static int EXPIRACION_REGISTRO = 300000;  
	private final static int MAX = 999999;
	private final static int MIN = 100000;
	
	static Map<String,RegistroTemporal> usuariosPendientes;
	
	static {
		usuariosPendientes = new HashMap<>();
	}
	
	/**
	 * Añade al Map el usuario <<user>> si no hay uno que ya tenga el mismo correo
	 * @param user Contiene el correo, nombre y contraseña (hash) a introducir en la base de datos
	 * @return el código si se ha podido añadir el usuario, null en caso contrario
	 */
	public static Integer anadirUsuario(UsuarioVO user) {
		Integer codigo = null;
		if (!usuariosPendientes.containsKey(user.getCorreo())) {
			codigo = (int) ((Math.random() * (MAX - MIN)) + MIN);;
			AlarmaRegistro alarm = new AlarmaRegistro(user.getCorreo());
			Timer t = new Timer(EXPIRACION_REGISTRO,alarm);
			RegistroTemporal rt = new RegistroTemporal(user,t,codigo);
			usuariosPendientes.put(user.getCorreo(),rt);
			t.start();
		}
		return codigo;
	}
	
	
	/**
	 * 
	 * @param correo
	 * @param codigo
	 * @return
	 */
	public static String confirmarRegistro(String correo, Integer codigo) {
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
