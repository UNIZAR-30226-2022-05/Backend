package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlarmaReestablecerContrasenna  implements ActionListener {
	
	private String correo;
	
	public AlarmaReestablecerContrasenna(String correo) {
		this.correo = correo;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GestorContrasennas.peticiones.get(correo).getTimer().stop();
		GestorContrasennas.peticiones.remove(correo);
	}

}
