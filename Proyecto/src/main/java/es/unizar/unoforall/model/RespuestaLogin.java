package es.unizar.unoforall.model;

import java.util.UUID;

public class RespuestaLogin {
	private boolean exito;
	private String errorInfo;
	private UUID sesionID;
	
	public RespuestaLogin(boolean exito, String errorInfo, UUID sessionID) {
		super();
		this.exito = exito;
		this.errorInfo = errorInfo;
		this.sesionID = sessionID;
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

	public UUID getSesionID() {
		return sesionID;
	}

	public void setSesionID(UUID sessionID) {
		this.sesionID = sessionID;
	}
	
	
}
