package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.UUID;

import javax.swing.Timer;

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
