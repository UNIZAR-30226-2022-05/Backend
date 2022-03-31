package es.unizar.pruebaCliente.model.salas;

import java.util.UUID;

import es.unizar.pruebaCliente.model.UsuarioVO;

public class NotificacionSala {
	private UUID salaID;
	private UsuarioVO remitente;
	
	public NotificacionSala() {
		
	}
	
	public NotificacionSala(UUID salaID, UsuarioVO remitente) {
		super();
		this.salaID = salaID;
		this.remitente = remitente;
	}

	public UUID getSalaID() {
		return salaID;
	}

	public void setSalaID(UUID salaID) {
		this.salaID = salaID;
	}

	public UsuarioVO getRemitente() {
		return remitente;
	}

	public void setRemitente(UsuarioVO remitente) {
		this.remitente = remitente;
	}

	@Override
	public String toString() {
		return "NotificacionSala [salaID=" + salaID + ", remitente=" + remitente + "]";
	}
}
