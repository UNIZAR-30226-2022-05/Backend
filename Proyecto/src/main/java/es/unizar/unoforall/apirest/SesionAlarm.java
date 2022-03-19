package es.unizar.unoforall.apirest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.UUID;

//import javax.swing.Timer;

public class SesionAlarm implements ActionListener {
	
	private UUID miSesion;
	private Map<UUID,Sesion> listaGlobal;
	
	public SesionAlarm(UUID sesion, Map<UUID,Sesion> lista) {
		miSesion = sesion;
		listaGlobal = lista;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/*Sesion sesion = listaGlobal.get(miSesion);
		Timer timer = sesion.getMiTimer();
		timer.stop();
		sesion.setMiTimer(timer);*/
		listaGlobal.remove(miSesion);
		
	}
}
