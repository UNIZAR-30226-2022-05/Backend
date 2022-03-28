package es.unizar.unoforall.model;

import java.util.UUID;

public class RespuestaLogin {
	private boolean exito;
	private String errorInfo;
	private UUID claveInicio;
	private UUID usuarioID;
	
	public RespuestaLogin(boolean exito, String errorInfo, UUID claveInicio, UUID usuarioID) {
		super();
		this.exito = exito;
		this.errorInfo = errorInfo;
		this.claveInicio = claveInicio;
		this.usuarioID = usuarioID;
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

	public UUID getClaveInicio() {
		return claveInicio;
	}

	public void setClaveInicio(UUID claveInicio) {
		this.claveInicio = claveInicio;
	}
	
	public UUID getUsuarioID() {
		return usuarioID;
	}

	public void setUsuarioID(UUID usuarioID) {
		this.usuarioID = usuarioID;
	}

	@Override
	public String toString() {
		return "RespuestaLogin [exito=" + exito + ", errorInfo=" + errorInfo + ", claveInicio=" + claveInicio
				+ ", usuarioID=" + usuarioID + "]";
	}

	
}
