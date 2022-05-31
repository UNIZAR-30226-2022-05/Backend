package es.unizar.unoforall.model.salas;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.partidas.RespuestaVotacionPausa;

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
	private HashMap<UUID, Object> participantesAck;
	private HashMap<UUID, Integer> participantesAckFallidos;
	private static final int MAX_FALLOS_ACK = 5;
	
	//Conjunto de participantes con el indicador de si están listos o no
	private HashMap<UUID, Boolean> participantesVotoAbandono;
	private boolean enPausa;
	
	private final static int TIMEOUT_ACK = 1000;	//1 segundo
	
	private final Object LOCK = new Object();
	
	private static final Object LOCK_STATIC = new Object();
	
	private Sala() {
		
	}
	
	public Sala(String mensajeError) {
		participantes = new HashMap<>();
		participantes_listos = new HashMap<>();
		participantesVotoAbandono = new HashMap<>();
		participantesAck = new HashMap<>();
		participantesAckFallidos = new HashMap<>();
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
	
	public void setEnPartidaExterno(boolean enPartida) {
		synchronized (LOCK) {
			this.setEnPartida(enPartida);
		}
	}
	
	private void setEnPartida(boolean enPartida) {
		if (this.enPartida != enPartida) {
			this.enPartida = enPartida;
			
			if (this.enPartida) {  // comienza una partida
				System.out.println("--- Comienza una partida");
				if (!isEnPausa()) {
					List<UUID> jugadoresID = new ArrayList<>();
					participantes.forEach((k,v) -> jugadoresID.add(k));
					Collections.shuffle(jugadoresID); 
					this.partida = new Partida(jugadoresID, configuracion, salaID);
				} else {
					System.out.println("--- Termina una pausa");
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
		synchronized (LOCK) {
			if (isEnPausa()) {
				participantesAck.putIfAbsent(participante.getId(), null);
				participantesAckFallidos.putIfAbsent(participante.getId(), 0);
				return false;
			}
			
			if(participantes.size() < configuracion.getMaxParticipantes()) {
				participantes.putIfAbsent(participante.getId(), participante);
				participantes_listos.putIfAbsent(participante.getId(), false);
				participantesAck.putIfAbsent(participante.getId(), null);
				participantesAckFallidos.putIfAbsent(participante.getId(), 0);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private void eliminarParticipanteInterno(UUID participanteID) {
		participantes.remove(participanteID);
		participantes_listos.remove(participanteID);
		ack(participanteID);
		participantesAck.remove(participanteID);
		participantesAckFallidos.remove(participanteID);
	}
	
	// Para eliminar un participante definitivamente mientras la partida está
	// pausada (también se elimimnará definitivamente si ha pulsado 'listo' y
	// luego se ha desconectado)
	public void eliminarParticipanteDefinitivamente(UUID participanteID) {
		synchronized (LOCK) {
			if (isEnPausa()) {
				if(participantes.containsKey(participanteID)) {
					eliminarParticipanteInterno(participanteID);
					participantesVotoAbandono.remove(participanteID);
					partida.expulsarJugador(participanteID);
					
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
	}
	
	public void eliminarParticipante(UUID participanteID) { 
		synchronized (LOCK) {
			participantesAck.remove(participanteID);
			participantesAckFallidos.remove(participanteID);
			if (isEnPausa()) {
				if(participantes_listos.containsKey(participanteID)
							&& participantes_listos.get(participanteID)) {
					eliminarParticipanteInterno(participanteID);
					partida.expulsarJugador(participanteID);
				}
				return;
			}
			
			if(participantes.containsKey(participanteID)) {
				eliminarParticipanteInterno(participanteID);
				participantesVotoAbandono.remove(participanteID);
				
				if (participantes.size() == 0) {
					return;
				}
				
				if (this.enPartida)	 {
					partida.expulsarJugador(participanteID);
					
					boolean todosListos = true;
					for (Map.Entry<UUID, Boolean> entry : participantesVotoAbandono.entrySet()) {
						if (entry.getValue() == false) { 
							todosListos = false; 
						}
					}
					if (todosListos) {
						setEnPausa(todosListos);
					}
					
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
	}
	
	// Devuelve true si todos los participantes ya están listos, y por tanto la
	// partida ha comenzado
	public boolean nuevoParticipanteListo(UUID participanteID) {
		synchronized (LOCK) {
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
		if (isEnPausa() || isEnPartida()) {
			return false;
		}
		
		if (getConfiguracion().isEsPublica()
				&& numParticipantes() < getConfiguracion().getMaxParticipantes()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public RespuestaVotacionPausa setParticipantesVotoAbandono(UUID participanteID) {
		synchronized (LOCK) {
			if(participantesVotoAbandono.containsKey(participanteID)) {
				participantesVotoAbandono.put(participanteID, true);
			}
			
			return getParticipantesVotoAbandono();
		}
	}
	
	public RespuestaVotacionPausa getParticipantesVotoAbandonoExterno() {
		synchronized (LOCK) {
			return getParticipantesVotoAbandono();
		}
	}
	
	private RespuestaVotacionPausa getParticipantesVotoAbandono() {
		int numListos = (int)participantesVotoAbandono.values()
								.stream().filter(listo -> listo).count();
		if (numListos == participantesVotoAbandono.size()) {
			setEnPausa(true);
		}
		return new RespuestaVotacionPausa(numListos, participantesVotoAbandono.size());
	}
	
	
	public boolean isEnPausa() {
		return enPausa;
	}

	private void setEnPausa(boolean enPausa) {
		if (this.enPausa != enPausa && this.enPartida) {
			this.enPausa = enPausa;
			
			if (this.enPausa) {  // comienza una pausa
				cancelTimer(salaID);
				System.out.println("--- Comienza una pausa");
				setEnPartida(false);
			}
		}
	}
	
	
	
	

	@Override
	public String toString() {
		synchronized (LOCK) {
			return "Sala [noExiste=" + noExiste + ", error=" + error + ", configuracion=" + configuracion + ", enPartida="
					+ enPartida + ", partida=" + partida + ", participantes=" + participantes + ", participantes_listos="
					+ participantes_listos + "]";
		}
	}

	public boolean isNoExiste() {
		return noExiste;
	}

	public String getError() {
		return error;
	}

	private void setError(String error) {
		this.error = error;
	}

	public Partida getPartida() {
		return partida;
	}

	
	public Sala getSalaAEnviar() {
		synchronized (LOCK) {
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
			salaResumida.ultimaPartidaJugada  = ultimaPartidaJugada;
			
			//Identificador de cada usuario con su VO
			salaResumida.participantes = participantes;
			//Conjunto de participantes con el indicador de si están listos o no
			salaResumida.participantes_listos = participantes_listos;
			
			salaResumida.enPausa = enPausa;
			
			salaResumida.salaID = salaID;
			
			return salaResumida;
		}
	}

	public PartidaJugada getUltimaPartidaJugada() {
		return ultimaPartidaJugada;
	}

	public void setUltimaPartidaJugada(PartidaJugada ultimaPartidaJugada) {
		synchronized (LOCK) {
			this.ultimaPartidaJugada = ultimaPartidaJugada;
		}
	}

	public UUID getSalaID() {
		return salaID;
	}

	public void setSalaID(UUID salaID) {
		synchronized (LOCK) {
			this.salaID = salaID;
		}
	}

	public void ack(UUID usuarioID) {
		Timer timerAck = (Timer) participantesAck.get(usuarioID);
	
		if(timerAck != null) {
			timerAck.cancel();
		}
		
		participantesAck.put(usuarioID, null);
		
		participantesAckFallidos.put(usuarioID, 0);
    }
	
	public void ack_fallido(UUID usuarioID) {
		synchronized (LOCK) {
			if (participantesAckFallidos.containsKey(usuarioID)) {
				int numFallosACK = participantesAckFallidos.get(usuarioID) + 1; 
				participantesAckFallidos.put(usuarioID, numFallosACK);
				
				if (numFallosACK >= MAX_FALLOS_ACK) {
					System.out.println("Cliente desconectado por desconexión");
					desconectarUsuario(usuarioID);
				}
			}
		}
	}
	
	public void initAckTimers() {
            if(0==0)return;
		synchronized (LOCK) {
			for (UUID usuarioID : participantesAck.keySet()) {
//				if(isEnPartida() && new Random().nextBoolean()) {
//					SocketController.desconectarUsuario(usuarioID);
//					return;
//				}
				Timer timerAck = (Timer) participantesAck.get(usuarioID);
				
				if(timerAck != null) {
					timerAck.cancel();
				}
				
				Object alarm = newAlarmaACK(this, usuarioID);
				Timer t = new Timer();
				t.schedule((TimerTask)alarm, TIMEOUT_ACK);
				participantesAck.put(usuarioID, t);
			}
		}
	}
	
	private static Method desconectarUsuario = null;
	public static void desconectarUsuario(UUID usuarioID) {
		synchronized (LOCK_STATIC) {
			try {
				if(desconectarUsuario == null){
					desconectarUsuario = Class.forName("es.unizar.unoforall.sockets.SocketController")
							.getMethod("desconectarUsuario", UUID.class);
				}
				desconectarUsuario.invoke(null, usuarioID);
			}catch(Exception ex){
				ex.printStackTrace();
				System.out.println("------------------------ Error 1");
			}
		}
	}
	
	private static Constructor newAlarmaACK = null;
    public static Object newAlarmaACK(Sala sala, UUID usuarioID){
    	synchronized (LOCK_STATIC) {
    		try{
                if(newAlarmaACK == null){
                	newAlarmaACK = Class.forName("es.unizar.unoforall.gestores.AlarmaACK")
                            .getConstructor(Sala.class, UUID.class);
                }
                return newAlarmaACK.newInstance(sala, usuarioID);
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("------------------------ Error 2");
                return null;
            }
		}
    }
    
	
	private static Method cancelTimer = null;
    public static void cancelTimer(UUID salaID){
    	synchronized (LOCK_STATIC) {
    		try{
                if(cancelTimer == null){
                    cancelTimer = Class.forName("es.unizar.unoforall.gestores.GestorSalas")
                            .getDeclaredMethod("cancelTimer", UUID.class);
                }
                cancelTimer.invoke(null, salaID);
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("------------------------ Error 3");
            }
		}
    }
    
}
