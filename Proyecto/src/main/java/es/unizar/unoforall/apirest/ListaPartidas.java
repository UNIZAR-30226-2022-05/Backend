package es.unizar.unoforall.apirest;

import java.util.ArrayList;

import es.unizar.unoforall.model.PartidasAcabadasVO;

/**
 * <expirado> 	true si la sesión ha expirado, y false en caso contrario.
 * <error>		"null" si no ha habido ningún error. Si ha ocurrido alguno, se informa por este String.
 * <partidas>	una lista de PartidasAcabadasVO.
 */
public class ListaPartidas {
	private boolean expirado;
	private String error = "null";
	private ArrayList<PartidasAcabadasVO> partidas = null;
	
	public ListaPartidas(boolean expirado) {
		this.expirado = expirado;	
		partidas = new ArrayList<PartidasAcabadasVO>();
	}

	public boolean isExpirado() {
		return expirado;
	}

	public void setExpirado(boolean expirado) {
		this.expirado = expirado;
	}

	public ArrayList<PartidasAcabadasVO> getPartidas() {
		return partidas;
	}

	public void setPartidas(ArrayList<PartidasAcabadasVO> partidas) {
		this.partidas = partidas;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
