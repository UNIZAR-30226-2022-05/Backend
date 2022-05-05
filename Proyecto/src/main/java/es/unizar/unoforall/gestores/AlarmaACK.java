package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;


public class AlarmaACK extends TimerTask {
	
	private UUID salaID;
	
	public AlarmaACK(UUID salaID) {
		this.salaID = salaID;
	}

	@Override
	public void run() {
		try {
			System.out.println("Partida reenviada por falta de ACK");
			GestorSesiones.getApiInterna().sendObject("/app/salas/actualizar/" + salaID, "vacio");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
