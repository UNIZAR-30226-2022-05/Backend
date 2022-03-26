package es.unizar.unoforall.model.salas;


import java.util.HashMap;

import es.unizar.unoforall.model.UsuarioVO;

public class Sala {	
	private ConfigSala configuracion;
	
	private boolean enPartida;
	
	//Conjunto de participantes con el indicador de si están listos o no
	private HashMap<UsuarioVO, Boolean> participantes;
	
	public Sala(ConfigSala configuracion) {
		super();
		this.configuracion = configuracion;
		this.setEnPartida(false);
		
		participantes = new HashMap<>();
	}

	public ConfigSala getConfiguracion() {
		return configuracion;
	}

	public boolean isEnPartida() {
		return enPartida;
	}

	public void setEnPartida(boolean enPartida) {
		this.enPartida = enPartida;
	}
	
	// Devuelve false si no es posible añadir un nuevo participante
	public boolean nuevoParticipante(UsuarioVO participante) {
		if(participantes.size() < configuracion.getMaxParticipantes()) {
			participantes.putIfAbsent(participante, false);
			return true;
		} else {
			return false;
		}
	}
	
	public void nuevoParticipanteListo(UsuarioVO participante) {
		if(participantes.containsKey(participante)) {
			participantes.put(participante, true);
		}
	}

	public HashMap<UsuarioVO, Boolean> getParticipantes() {
		return participantes;
	}

	@Override
	public String toString() {
		return "Sala [configuracion=" + configuracion + ", enPartida=" + enPartida + ", participantes=" + participantes
				+ "]";
	}
}
