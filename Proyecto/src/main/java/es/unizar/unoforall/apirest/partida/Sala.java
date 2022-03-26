package es.unizar.unoforall.apirest.partida;


import java.util.HashSet;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partida.ConfigSala;

public class Sala {	
	private ConfigSala configuracion;
	
	private boolean enPartida;
	private HashSet<UsuarioVO> participantes;
	
	public Sala(ConfigSala configuracion, UsuarioVO creador) {
		super();
		this.configuracion = configuracion;
		this.setEnPartida(false);
		
		participantes = new HashSet<UsuarioVO>();
		participantes.add(creador);
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
	
	
}
