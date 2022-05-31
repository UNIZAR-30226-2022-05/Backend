package es.unizar.unoforall.gestores.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlarmaRegistro implements ActionListener {
	
	private String correo;
	
	public AlarmaRegistro(String correo) {
		this.correo = correo;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GestorRegistros.usuariosPendientes.get(correo).getTimer().stop();
		GestorRegistros.usuariosPendientes.remove(correo);
	}

}
