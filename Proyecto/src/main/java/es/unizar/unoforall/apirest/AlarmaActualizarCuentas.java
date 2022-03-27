package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlarmaActualizarCuentas implements ActionListener {
	
	private String correo;
	
	public AlarmaActualizarCuentas(String correo) {
		this.correo = correo;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GestorActualizaCuentas.peticiones.get(correo).getTimer().stop();
		GestorActualizaCuentas.peticiones.remove(correo);
	}

}
