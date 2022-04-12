package es.unizar.unoforall.gestores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import es.unizar.unoforall.sockets.SocketController;

public class AlarmaTurnoIA implements ActionListener {
	
	private SocketController sController;
	private UUID salaID;
	
	public AlarmaTurnoIA(SocketController sController, UUID salaID) {
		this.sController = sController;
		this.salaID = salaID;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			sController.turnoPartidaIA(salaID, "");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
