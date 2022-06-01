package es.unizar.unoforall.model.salas;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RespuestaSalas {
	private boolean exito;
	private Map<UUID,Sala> salas;
	
	public RespuestaSalas() {
		salas = new HashMap<>();
		exito = false;
	}
	
	public RespuestaSalas(Map<UUID,Sala> salas) {
		this();
		this.salas = salas;
		this.exito = true;
	}

	public Map<UUID,Sala> getSalas() {
		return salas;
	}

	public void setSalas(Map<UUID,Sala> salas) {
		this.salas = salas;
	}

	public boolean isExito() {
		return exito;
	}
	
	public RespuestaSalas getRespuestaAEnviar() {
		RespuestaSalas respuestaResumida = new RespuestaSalas();
		
		respuestaResumida.exito = this.exito;

		respuestaResumida.salas = new HashMap<>();
		this.salas.forEach((uuid, sala) -> 
			respuestaResumida.salas.put(uuid, sala.getSalaAEnviar()));
		
		return respuestaResumida;
	}
}
