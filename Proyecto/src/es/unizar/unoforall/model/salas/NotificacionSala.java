package es.unizar.unoforall.model.salas;

import java.util.Objects;
import java.util.UUID;

import es.unizar.unoforall.model.UsuarioVO;

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

	@Override
	public int hashCode() {
		return Objects.hash(remitente.getNombre(), salaID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificacionSala other = (NotificacionSala) obj;
		return Objects.equals(remitente.getNombre(), other.remitente.getNombre()) && Objects.equals(salaID, other.salaID);
	}
}
