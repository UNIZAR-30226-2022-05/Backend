package es.unizar.unoforall.model.salas;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.partidas.PartidaJugada;

public class Sala {	
	//Para devolver una sala que no existe
	private boolean noExiste;
	private String error;
	
	private UUID salaID;
	private ConfigSala configuracion;
	
	private boolean enPartida;
	private Partida partida;
	private PartidaJugada ultimaPartidaJugada;
	
	//Identificador de cada usuario con su VO
	private HashMap<UUID, UsuarioVO> participantes;
	//Conjunto de participantes con el indicador de si están listos o no
	private HashMap<UUID, Boolean> participantes_listos;
	
	//Conjunto de participantes con el indicador de si están listos o no
	private HashMap<UUID, Boolean> participantesVotoAbandono;
	private boolean enPausa;
	private Partida partidaPausada;
	

	private Sala() {
		
	}
	
	public Sala(String mensajeError) {
		participantes = new HashMap<>();
		participantes_listos = new HashMap<>();
		participantesVotoAbandono = new HashMap<>();
		noExiste = true;
		setError(mensajeError);
		partida = null;
	}
	
	public Sala(ConfigSala configuracion, UUID salaID) {
		this("");
		this.configuracion = configuracion;
		this.setEnPartida(false);
		this.noExiste = false;
		this.salaID = salaID;
	}
	
	public void setEnPartida(boolean enPartida) {
		if (this.enPartida != enPartida) {
			this.enPartida = enPartida;
			
			if (this.enPartida) {  // comienza una partida
				
				if (!isEnPausa()) {
					List<UUID> jugadoresID = new ArrayList<>();
					participantes.forEach((k,v) -> jugadoresID.add(k));
					this.partida = new Partida(jugadoresID, configuracion, salaID);
				} else {
					this.partida = this.partidaPausada;
					this.enPausa = false;
				}
					
				participantes.forEach((k,v) -> participantesVotoAbandono.put(k, false));
				
			} else {			   // termina una partida
				for (Map.Entry<UUID, Boolean> entry : participantes_listos.entrySet()) {
					entry.setValue(false);
				}
			}
		}
		
	}

	public boolean isEnPartida() {
		return enPartida;
	}
	
	public ConfigSala getConfiguracion() {
		return configuracion;
	}
	
	// Devuelve false si no es posible añadir un nuevo participante
	public boolean nuevoParticipante(UsuarioVO participante) {
		if (isEnPausa()) {
			return false;
		}
		
		if(participantes.size() < configuracion.getMaxParticipantes()) {
			participantes.putIfAbsent(participante.getId(), participante);
			participantes_listos.putIfAbsent(participante.getId(), false);
			return true;
		} else {
			return false;
		}
	}
	
	// Para eliminar un participante definitivamente mientras la partida está
	// pausada (también se elimimnará definitivamente si ha pulsado 'listo' y
	// luego se ha desconectado)
	public void eliminarParticipanteDefinitivamente(UUID participanteID) {
		if (isEnPausa()) {
			if(participantes.containsKey(participanteID)) {
				participantes.remove(participanteID);
				participantes_listos.remove(participanteID);
				partidaPausada.expulsarJugador(participanteID);
				
				boolean todosListos = true;
				for (Map.Entry<UUID, Boolean> entry : participantes_listos.entrySet()) {
					if (entry.getValue() == false) { 
						todosListos = false; 
					}
				}
				if (todosListos) {
					setEnPartida(true);
				}
			}
		}
	}
	
	public void eliminarParticipante(UUID participanteID) {
		if (isEnPausa()) {
			if(participantes_listos.containsKey(participanteID)
						&& participantes_listos.get(participanteID)) {
				participantes.remove(participanteID);
				participantes_listos.remove(participanteID);
				partidaPausada.expulsarJugador(participanteID);
			}
			return;
		}
		
		if(participantes.containsKey(participanteID)) {
			participantes.remove(participanteID);
			participantes_listos.remove(participanteID);
			
			if (this.enPartida)	 {
				partida.expulsarJugador(participanteID);
			} else {	//Si se va un jugador no listo, y el resto ya lo están 
						//	-> se empieza la partida
				boolean todosListos = true;
				for (Map.Entry<UUID, Boolean> entry : participantes_listos.entrySet()) {
					if (entry.getValue() == false) { 
						todosListos = false; 
					}
				}
				if (todosListos) {
					setEnPartida(true);
				}
			}
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
	
	public UsuarioVO getParticipante(UUID participanteID) {
		return participantes.get(participanteID);
	}
	
	public int numParticipantes() {
		return participantes.size();
	}
	
	public boolean puedeUnirse() {
		if (isEnPausa()) {
			return false;
		}
		
		if (getConfiguracion().isEsPublica()
				&& numParticipantes() < getConfiguracion().getMaxParticipantes() 
				&& !isEnPartida()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	public HashMap<UUID, Boolean> getParticipantesVotoAbandono() {
		return participantesVotoAbandono;
	}
	
	public HashMap<UUID, Boolean> setParticipantesVotoAbandono(UUID participanteID) {
		if(participantesVotoAbandono.containsKey(participanteID)) {
			participantesVotoAbandono.put(participanteID, true);
			
			boolean todosListos = true;
			for (Map.Entry<UUID, Boolean> entry : participantesVotoAbandono.entrySet()) {
				if (entry.getValue() == false) { 
					todosListos = false; 
				}
			}
			if (todosListos) {
				setEnPausa(todosListos);
			}
		}
		return getParticipantesVotoAbandono();
	}
	
	public boolean isEnPausa() {
		return enPausa;
	}

	public void setEnPausa(boolean enPausa) {
		if (this.enPausa != enPausa && this.enPartida) {
			this.enPausa = enPausa;
			
			if (this.enPausa) {  // comienza una pausa
				this.partidaPausada = this.partida;
				setEnPartida(false);
			}
		}
		
	}
	
	
	
	

	@Override
	public String toString() {
		return "Sala [noExiste=" + noExiste + ", error=" + error + ", configuracion=" + configuracion + ", enPartida="
				+ enPartida + ", partida=" + partida + ", participantes=" + participantes + ", participantes_listos="
				+ participantes_listos + "]";
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

	
	public Sala getSalaAEnviar() {
		Sala salaResumida = new Sala();
		
		salaResumida.noExiste = noExiste;
		salaResumida.error = error;
		
		salaResumida.configuracion = configuracion;
		
		salaResumida.enPartida = enPartida;
		
		if (partida != null) {
			salaResumida.partida = partida.getPartidaAEnviar();
		} else {
			salaResumida.partida = null;
		}
		
		//Identificador de cada usuario con su VO
		salaResumida.participantes = participantes;
		//Conjunto de participantes con el indicador de si están listos o no
		salaResumida.participantes_listos = participantes_listos;
		
		return salaResumida;
	}

	public PartidaJugada getUltimaPartidaJugada() {
		return ultimaPartidaJugada;
	}

	public void setUltimaPartidaJugada(PartidaJugada ultimaPartidaJugada) {
		this.ultimaPartidaJugada = ultimaPartidaJugada;
	}

	
}
