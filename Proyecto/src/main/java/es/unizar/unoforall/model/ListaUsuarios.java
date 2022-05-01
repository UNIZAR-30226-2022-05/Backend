package es.unizar.unoforall.model;

import java.util.ArrayList;

/**
 * <expirado> 	true si la sesión ha expirado, y false en caso contrario.
 * <error>		"null" si no ha habido ningún error. Si ha ocurrido alguno, se informa por este String.
 * <usuarios>	una lista de UsuariosVO.
 */
public class ListaUsuarios {
	private boolean expirado;
	private String error = "null";
	private ArrayList<UsuarioVO> usuarios = null;
	
	public ListaUsuarios(boolean expirado) {
		this.expirado = expirado;	
		usuarios = new ArrayList<UsuarioVO>();
	}

	public boolean isExpirado() {
		return expirado;
	}

	public void setExpirado(boolean expirado) {
		this.expirado = expirado;
	}

	public ArrayList<UsuarioVO> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(ArrayList<UsuarioVO> usuarios) {
		this.usuarios = usuarios;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
