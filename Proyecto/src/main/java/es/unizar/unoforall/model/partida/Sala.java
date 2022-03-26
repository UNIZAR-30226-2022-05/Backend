package es.unizar.unoforall.model.partida;

import java.util.UUID;

public class Sala {
	private UUID idSala;
	private boolean publica;
	private ConfigPartida configuracion;
	
	public Sala(UUID idSala, boolean publica, ConfigPartida configuracion) {
		super();
		this.idSala = idSala;
		this.publica = publica;
		this.configuracion = configuracion;
	}

	public UUID getIdSala() {
		return idSala;
	}


	public boolean isPublica() {
		return publica;
	}


	public ConfigPartida getConfiguracion() {
		return configuracion;
	}
	
	
	
}
