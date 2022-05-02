package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.model.salas.Sala;


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
				GestorSesiones.getApiInterna().sendObject("/app/partidas/turnosIA/" + salaID, "vacio");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
