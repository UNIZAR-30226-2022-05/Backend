package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;
import me.i2000c.web_utils.client.RestClient;


public class AlarmaFinTurno extends TimerTask {
	
	private UUID salaID;
	
	public AlarmaFinTurno(UUID salaID) {
		this.salaID = salaID;
	}

	@Override
	public void run() {
		try {
			System.out.println("Turno terminado forzosamente por límite de tiempo");
                        RestClient client = GestorSesiones.getApiInterna().getRestClient();
                        client.openConnection("/app/partidas/saltarTurno/" + salaID);
                        client.receiveObject(String.class, null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
