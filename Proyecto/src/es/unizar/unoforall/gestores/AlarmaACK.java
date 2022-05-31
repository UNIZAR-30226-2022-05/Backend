package es.unizar.unoforall.gestores;

import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.model.salas.Sala;
import me.i2000c.web_utils.client.RestClient;


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
                        RestClient client = GestorSesiones.getApiInterna().getRestClient();
                        client.openConnection("/app/salas/actualizar/" + sala.getSalaID());
                        client.receiveObject(String.class, null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
