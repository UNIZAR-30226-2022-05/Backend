package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.sockets.SocketController;

public class AlarmaTurnoIA extends TimerTask {
	
	private SocketController sController;
	private UUID salaID;
	
	public AlarmaTurnoIA(SocketController sController, UUID salaID) {
		this.sController = sController;
		this.salaID = salaID;
	}

	@Override
	public void run() {
		try {
			sController.turnoPartidaIA(salaID, "");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
