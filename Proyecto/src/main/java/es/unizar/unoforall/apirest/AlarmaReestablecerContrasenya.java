package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlarmaReestablecerContrasenya  implements ActionListener {
	
	private String correo;
	
	public AlarmaReestablecerContrasenya(String correo) {
		this.correo = correo;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GestorContrasenyas.peticiones.get(correo).getTimer().stop();
		GestorContrasenyas.peticiones.remove(correo);
	}

}
