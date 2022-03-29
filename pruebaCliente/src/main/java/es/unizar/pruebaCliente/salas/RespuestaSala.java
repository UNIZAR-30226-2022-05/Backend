package es.unizar.pruebaCliente.salas;

import java.util.UUID;

public class RespuestaSala {
	private boolean exito;
	private String errorInfo;
	private UUID salaID;
	
	public RespuestaSala(boolean exito, String errorInfo, UUID salaID) {
		super();
		this.exito = exito;
		this.errorInfo = errorInfo;
		this.salaID = salaID;
	}
	
	public boolean isExito() {
		return exito;
	}
	public void setExito(boolean exito) {
		this.exito = exito;
	}
	public String getErrorInfo() {
		return errorInfo;
	}
	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	public UUID getSalaID() {
		return salaID;
	}
	public void setSalaID(UUID salaID) {
		this.salaID = salaID;
	}
	
}
