package es.unizar.unoforall.apirest;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

import es.unizar.unoforall.utils.Mail;

public class GestorContrasennas {
	private final static int EXPIRACION_REGISTRO = 5*60000;  
	
	private final static int MAX_CODIGO = 999999;
	private final static int MIN_CODIGO = 100000;
	static Map<String,SolicitudCambioContrasenna> peticiones;
	
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
	public static String anyadirPeticion(String correo) {
		String error = "null";
		if (peticiones.containsKey(correo)) {
			peticiones.get(correo).getTimer().stop();
			peticiones.remove(correo);
		}
		int codigo = (int) ((Math.random() * (MAX_CODIGO - MIN_CODIGO)) + MIN_CODIGO);
		boolean exitoMail = Mail.sendMail(correo, 
				"Solicitud de cambio de contraseña de la cuenta en UNOForAll", 
				"Su código de verificación es: " + Integer.toString(codigo) +
				".\nRecuerde que si tarda más de 5 minutos tendrá que volver a "
				+ "solicitarla (podrá usar el mismo correo)");
		if (!exitoMail)	{
			System.err.println("No se ha encontrado el archivo credenciales.properties");
			error = "Fallo en el servidor: no se pudo enviar el correo";
		} else {
			AlarmaReestablecerContrasenna alarm = new AlarmaReestablecerContrasenna(correo);
			Timer t = new Timer(EXPIRACION_REGISTRO,alarm);
			SolicitudCambioContrasenna scc = new SolicitudCambioContrasenna(t,codigo);
			peticiones.put(correo,scc);
			t.start();
		}
		return error;
	}
	
	public static String confirmarCodigo(String correo, Integer codigo) {
		String error = "null";
		if (peticiones.containsKey(correo)) {
			if (peticiones.get(correo).getCodigo()==codigo) {
				peticiones.get(correo).getTimer().stop();
				peticiones.remove(correo);
			} else {
				error = "Ha introducido un código erróneo. Vuelva a mirarlo en el correo.";
			}
		} else {
			anyadirPeticion(correo);
			error = "Su petición ya ha expirado, se le ha vuelto a enviar otro código.";
		}
		return error;
	}
}
