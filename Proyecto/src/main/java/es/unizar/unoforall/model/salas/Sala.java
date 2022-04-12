package es.unizar.unoforall.model.salas;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Partida;

public class Sala {	
	//Para devolver una sala que no existe
	private boolean noExiste;
	private String error;
	
	private ConfigSala configuracion;
	
	private boolean enPartida;
	private Partida partida;
	
	//Identificador de cada usuario con su VO
	private HashMap<UUID, UsuarioVO> participantes;
	//Conjunto de participantes con el indicador de si están listos o no
	private HashMap<UUID, Boolean> participantes_listos;
	
	public Sala(String mensajeError) {
		participantes = new HashMap<>();
		participantes_listos = new HashMap<>();
		noExiste = true;
		setError(mensajeError);
		partida = null;
	}
	
	public Sala(ConfigSala configuracion) {
		this("");
		this.configuracion = configuracion;
		this.setEnPartida(false);
		this.noExiste = false;
	}
	
	public void setEnPartida(boolean enPartida) {
		if (this.enPartida != enPartida) {
			this.enPartida = enPartida;
			
			if (this.enPartida) {  // comienza una partida
				List<UUID> jugadoresID = new ArrayList<>();
				participantes.forEach((k,v) -> jugadoresID.add(k));
				this.partida = new Partida(jugadoresID, configuracion);
			} else {			   // termina una partida
				this.partida = null;
			}
		}
		
	}

	
	public ConfigSala getConfiguracion() {
		return configuracion;
	}

	public boolean isEnPartida() {
		return enPartida;
	}
	
	// Devuelve false si no es posible añadir un nuevo participante
	public boolean nuevoParticipante(UsuarioVO participante) {
		if(participantes.size() < configuracion.getMaxParticipantes()) {
			participantes.putIfAbsent(participante.getId(), participante);
			participantes_listos.putIfAbsent(participante.getId(), false);
			return true;
		} else {
			return false;
		}
	}
	
	
	public void eliminarParticipante(UUID participanteID) {
		if(participantes.containsKey(participanteID)) {
			participantes.remove(participanteID);
			participantes_listos.remove(participanteID);
			
			partida.expulsarJugador(participanteID);
		}
	}
	
	// Devuelve true si todos los participantes ya están listos, y por tanto la
	// partida ha comenzado
	public boolean nuevoParticipanteListo(UUID participanteID) {
		if(participantes.containsKey(participanteID)) {
			participantes_listos.put(participanteID, true);
			boolean todosListos = true;
			for (Map.Entry<UUID, Boolean> entry : participantes_listos.entrySet()) {
				if (entry.getValue() == false) { 
					todosListos = false; 
				}
			}
			if (todosListos) {
				setEnPartida(true);
			}
			return todosListos;
		} else {
			return false;
		}
	}
	
	// Devuelve un hashmap con el  usuarioID - UsuarioVO
	public boolean hayParticipante(UUID usuarioID) {
		if (participantes.containsKey(usuarioID)) {
			return true;
		} else {
			return false;
		}
	}
	
	// Devuelve un hashmap con el VO de cada usuario relacionado con si está o no preparado
	public HashMap<UsuarioVO, Boolean> getParticipantes() {
		HashMap<UsuarioVO, Boolean> result = new HashMap<>();
		participantes.forEach((k,v) -> result.put(v, participantes_listos.get(k)));
		return result;
	}
	
	public int numParticipantes() {
		return participantes.size();
	}
	
	public boolean puedeUnirse() {
		if (getConfiguracion().isEsPublica()
				&& numParticipantes() < getConfiguracion().getMaxParticipantes() 
				&& !isEnPartida()) {
			return true;
		} else {
			return false;
		}
	}
	

	@Override
	public String toString() {
		return "Sala [configuracion=" + configuracion + ", enPartida=" + enPartida + ", participantes=" + participantes
				+ "]";
	}

	public boolean isNoExiste() {
		return noExiste;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Partida getPartida() {
		return partida;
	}

}
