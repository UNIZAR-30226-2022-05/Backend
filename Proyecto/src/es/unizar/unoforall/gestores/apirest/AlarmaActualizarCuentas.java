package es.unizar.unoforall.gestores.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class AlarmaActualizarCuentas implements ActionListener {
	
	private UUID id;
	
	public AlarmaActualizarCuentas(UUID sessionID) {
		this.id = sessionID;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GestorActualizaCuentas.peticiones.get(id).getTimer().stop();
		GestorActualizaCuentas.peticiones.remove(id);
	}

}
