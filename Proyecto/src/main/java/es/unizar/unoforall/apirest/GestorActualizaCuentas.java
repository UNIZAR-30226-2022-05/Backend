package es.unizar.unoforall.apirest;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.utils.Mail;

public class GestorActualizaCuentas {
private final static int EXPIRACION_REGISTRO = 5*60000;  
	
	private final static int MAX_CODIGO = 999999;
	private final static int MIN_CODIGO = 100000;
	static Map<String,RegistroTemporal> peticiones;
	
	static {
		peticiones = new HashMap<>();
	}
	
	/**
	 * Función que asocia a un correo la petición de cambiar la contraseña de
	 * su cuenta asociada. Si ya había una petición previa sin resolver, se 
	 * elimina y se sustituye por una nueva
	 * @param correo contiene el correo asociado a la cuenta cuya contraseña se
	 * 		  quiere reestablecer.
	 */
	public static String anyadirPeticion(String correoViejo, String correoNuevo, String contrasenya, String nombre) {
		/*if (peticiones.containsKey(correo)) {
			peticiones.get(correo).getTimer().stop();
			peticiones.remove(correo);
		}*/
		String error = null;
		if (!GestorRegistros.usuariosPendientes.containsKey(correoNuevo) &&
									!peticiones.containsKey(correoNuevo) && 
									UsuarioDAO.getUsuario(correoNuevo)==null) {
			UsuarioVO usuario = new UsuarioVO(correoViejo,nombre,contrasenya);
			int codigo = (int) ((Math.random() * (MAX_CODIGO - MIN_CODIGO)) + MIN_CODIGO);
			Mail.sendMail(correoNuevo, 
					"Solicitud de actualización de la cuenta en UNOForAll", 
					"Su código de verificación es: " + Integer.toString(codigo) +
					".\nRecuerde que si tarda más de 5 minutos tendrá que volver a "
					+ "solicitarla (podrá usar el mismo correo, nombre y contraseña)");
			AlarmaActualizarCuentas alarm = new AlarmaActualizarCuentas(correoNuevo);
			Timer t = new Timer(EXPIRACION_REGISTRO,alarm);
			RegistroTemporal rt = new RegistroTemporal(usuario,t,codigo);
			peticiones.put(correoNuevo,rt);
			t.start();
		} else {
			error = "El nuevo correo ya está en uso por otra cuenta o registro.";
		}
		return error;
	}
	
	
	public static String confirmarCodigo(String correo, Integer codigo) {
		String error = null;
		if (peticiones.containsKey(correo)) {
			if (peticiones.get(correo).getCodigo()==codigo) {
				peticiones.get(correo).getTimer().stop();
				UsuarioDAO.actualizarCuenta(correo,peticiones.get(correo).getUsuario());
				peticiones.remove(correo);
			} else {
				error = "Ha introducido un código erróneo. Vuelva a mirarlo en el correo.";
			}
		} else {
			error = "Su petición ya ha expirado, vuelva a realizarla.";
		}
		return error;
	}
	
	public static String cancelarActualizacion(String correo) {
		String error = null;
		if (peticiones.containsKey(correo)) {
			peticiones.get(correo).getTimer().stop();
			peticiones.remove(correo);
		} else {
			error = "No hay una petición de actualización con este correo. Puede que ya haya aplicado o expirado.";
		}
		return error;
	}
}
