package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

//import javax.swing.Timer;

public class AlarmaSesion implements ActionListener {
	
	private UUID sesionID;
	
	public AlarmaSesion(UUID sesionID) {
		this.sesionID = sesionID;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Sesion " + sesionID + " caducada");
		GestorSesiones.obtenerSesion(sesionID).getTimer().stop();
		GestorSesiones.eliminarSesion(sesionID);
	}
}
