package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.model.salas.Sala;
import me.i2000c.web_utils.client.RestClient;


public class AlarmaTurnoIA extends TimerTask {
	
	private UUID salaID;
	
	public AlarmaTurnoIA(UUID salaID) {
		this.salaID = salaID;
	}

	@Override
	public void run() {
		try {
			Sala sala = GestorSalas.obtenerSala(salaID);
			if (sala != null && sala.isEnPartida() && !sala.isEnPausa()) {
                            RestClient client = GestorSesiones.getApiInterna().getRestClient();
                            client.openConnection("/app/partidas/turnosIA/" + salaID);
                            client.receiveObject(String.class, null);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
