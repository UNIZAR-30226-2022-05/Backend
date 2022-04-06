package es.unizar.unoforall.apirest;

import java.util.ArrayList;

import es.unizar.unoforall.model.HaJugadoVO;

/**
 * <expirado> 	true si la sesión ha expirado, y false en caso contrario.
 * <error>		"nulo" si no ha habido ningún error. Si ha ocurrido alguno, se informa por este String.
 * <usuarios>	una lista de UsuariosVO.
 */
public class ListaParticipantes {
	private boolean expirado;
	private String error = "nulo";
	private ArrayList<HaJugadoVO> participantes = null;
	
	public ListaParticipantes(boolean expirado) {
		this.expirado = expirado;	
		participantes = new ArrayList<HaJugadoVO>();
	}

	public boolean isExpirado() {
		return expirado;
	}

	public void setExpirado(boolean expirado) {
		this.expirado = expirado;
	}
	
	public ArrayList<HaJugadoVO> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(ArrayList<HaJugadoVO> participantes) {
		this.participantes = participantes;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
