package es.unizar.unoforall.model;

import java.util.UUID;

public class LoginResponse {
	private boolean exito;
	private String errorInfo;
	private UUID sessionID;
	
	public LoginResponse(boolean exito, String errorInfo, UUID sessionID) {
		super();
		this.exito = exito;
		this.errorInfo = errorInfo;
		this.sessionID = sessionID;
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

	public UUID getSessionID() {
		return sessionID;
	}

	public void setSessionID(UUID sessionID) {
		this.sessionID = sessionID;
	}
	
	
}
