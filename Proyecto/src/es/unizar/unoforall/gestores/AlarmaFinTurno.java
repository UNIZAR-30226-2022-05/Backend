package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;


public class AlarmaFinTurno extends TimerTask {
	
	private UUID salaID;
	
	public AlarmaFinTurno(UUID salaID) {
		this.salaID = salaID;
	}

	@Override
	public void run() {
		try {
			System.out.println("Turno terminado forzosamente por límite de tiempo");
			GestorSesiones.getApiInterna().sendObject("/app/partidas/saltarTurno/" + salaID, "vacio");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
