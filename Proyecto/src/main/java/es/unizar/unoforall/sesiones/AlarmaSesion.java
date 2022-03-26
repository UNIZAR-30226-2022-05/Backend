package es.unizar.unoforall.sesiones;

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
		Sesion s = GestorSesiones.obtenerSesion(sesionID);
		if (GestorSesiones.haySesionWS(sesionID)) {
			s.getTimer().restart();
		} else {
			System.out.println("Sesion " + sesionID + " caducada");
			s.getTimer().stop();
			GestorSesiones.eliminarSesion(sesionID);
		}
	}
}
