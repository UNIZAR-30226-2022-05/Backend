package es.unizar.unoforall.model;

import java.util.UUID;

public class PartidasAcabadasVO {
	private final UUID id;
	private Long fechaInicioPartida;
	private Long fechaFinPartida;
	private int numIas;
	private int modoJuego;
	
	public PartidasAcabadasVO(UUID id, Long fechaInicioPartida, Long fechaFinPartida, int numIas, int modoJuego) {
		super();
		if(id == null) {
			this.id = UUID.randomUUID();
		} else {
			this.id = id;
		}
		this.fechaInicioPartida = fechaInicioPartida;
		this.fechaFinPartida = fechaFinPartida;
		this.numIas = numIas;
		this.modoJuego = modoJuego;
	}
	
	public Long getFechaInicioPartida() {
		return fechaInicioPartida;
	}
	public void setFechaInicioPartida(Long fechaInicioPartida) {
		this.fechaInicioPartida = fechaInicioPartida;
	}
	public Long getFechaFinPartida() {
		return fechaFinPartida;
	}
	public void setFechaFinPartida(Long fechaFinPartida) {
		this.fechaFinPartida = fechaFinPartida;
	}
	public int getNumIas() {
		return numIas;
	}
	public void setNumIas(int numIas) {
		this.numIas = numIas;
	}
	public int getModoJuego() {
		return modoJuego;
	}
	public void setModoJuego(int modoJuego) {
		this.modoJuego = modoJuego;
	}
	public UUID getId() {
		return id;
	}

	@Override
	public String toString() {
		return "PartidasAcabadasVO [id=" + id + ", fechaInicioPartida=" + fechaInicioPartida + ", fechaFinPartida="
				+ fechaFinPartida + ", numIas=" + numIas + ", modoJuego=" + modoJuego + "]";
	}
	
	
}
