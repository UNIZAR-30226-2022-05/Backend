package es.unizar.pruebaCliente.model;

import java.util.UUID;

public class HaJugadoVO {
	private final UUID usuario;
	private UUID partida;
	private int usrsDebajo;
	private boolean haGanado;
	
	public HaJugadoVO(UUID usuario, UUID partida, int usrsDebajo, boolean haGanado) {
		this.usuario=usuario;
		this.partida=partida;
		this.usrsDebajo=usrsDebajo;
		this.haGanado=haGanado;
	}

	public UUID getPartida() {
		return partida;
	}

	public void setPartida(UUID partida) {
		this.partida = partida;
	}

	public int getUsrsDebajo() {
		return usrsDebajo;
	}

	public void setUsrsDebajo(int usrsDebajo) {
		this.usrsDebajo = usrsDebajo;
	}

	public boolean isHaGanado() {
		return haGanado;
	}

	public void setHaGanado(boolean haGanado) {
		this.haGanado = haGanado;
	}

	public UUID getUsuario() {
		return usuario;
	}
	
}
