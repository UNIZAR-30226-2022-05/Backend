package es.unizar.pruebaCliente;

import java.sql.Date;
import java.util.UUID;

public class PartidasAcabadasVO {
	private final UUID id;
	private Date fechaInicioPartida;
	private Date fechaFinPartida;
	private int numIas;
	private int modoJuego;
	
	public PartidasAcabadasVO(UUID id, Date fechaInicioPartida, Date fechaFinPartida, int numIas, int modoJuego) {
		super();
		this.id = id;
		this.fechaInicioPartida = fechaInicioPartida;
		this.fechaFinPartida = fechaFinPartida;
		this.numIas = numIas;
		this.modoJuego = modoJuego;
	}
	
	public Date getFechaInicioPartida() {
		return fechaInicioPartida;
	}
	public void setFechaInicioPartida(Date fechaInicioPartida) {
		this.fechaInicioPartida = fechaInicioPartida;
	}
	public Date getFechaFinPartida() {
		return fechaFinPartida;
	}
	public void setFechaFinPartida(Date fechaFinPartida) {
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
