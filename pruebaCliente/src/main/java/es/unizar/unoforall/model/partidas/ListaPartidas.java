package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;


/**
 * <expirado> 	true si la sesión ha expirado, y false en caso contrario.
 * <error>		"nulo" si no ha habido ningún error. Si ha ocurrido alguno, se informa por este String.
 * <partidas>	una lista de PartidasAcabadasVO.
 */
public class ListaPartidas {
	private boolean expirado;
	private String error = "nulo";
	private ArrayList<PartidaJugada> partidas = null;
	
	public ListaPartidas(boolean expirado) {
		this.expirado = expirado;	
		partidas = new ArrayList<PartidaJugada>();
	}

	public boolean isExpirado() {
		return expirado;
	}

	public void setExpirado(boolean expirado) {
		this.expirado = expirado;
	}

	public ArrayList<PartidaJugada> getPartidas() {
		return partidas;
	}

	public void setPartidas(ArrayList<PartidaJugada> partidas) {
		this.partidas = partidas;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
