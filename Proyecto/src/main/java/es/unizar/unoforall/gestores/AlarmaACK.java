package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.model.salas.Sala;


public class AlarmaACK extends TimerTask {
	
	private Sala sala;
	private UUID usuarioID;
	
	public AlarmaACK(Sala sala, UUID usuarioID) {
		this.sala = sala;
		this.usuarioID = usuarioID;
	}

	@Override
	public void run() {
		try {
			sala.ack_fallido(usuarioID);
			System.out.println("Partida reenviada por falta de ACK");
			GestorSesiones.getApiInterna().sendObject("/app/salas/actualizar/" + sala.getSalaID(), "vacio");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
