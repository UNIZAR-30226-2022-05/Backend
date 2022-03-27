package es.unizar.unoforall.model.salas;

import java.util.HashMap;
import java.util.UUID;

public class RespuestaSalas {
	private HashMap<UUID,Sala> salas;
	
	public RespuestaSalas(HashMap<UUID,Sala> salas) {
		this.setSalas(salas);
	}

	public HashMap<UUID,Sala> getSalas() {
		return salas;
	}

	public void setSalas(HashMap<UUID,Sala> salas) {
		this.salas = salas;
	}
}
