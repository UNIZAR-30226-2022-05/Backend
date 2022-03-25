package es.unizar.unoforall.model.partida;


public class ConfigPartida {
	public enum ModoJuego{Original, Attack, Parejas};
	
	private ModoJuego modoJuego;
	private ReglasEspeciales reglas;
	private int maxParticipantes;
	
	public ConfigPartida(ModoJuego modoJuego, ReglasEspeciales reglas, 
			int maxParticipantes) {
		super();
		this.modoJuego = modoJuego;
		this.reglas = reglas;
		this.maxParticipantes = maxParticipantes;
	}

	public ModoJuego getModoJuego() {
		return modoJuego;
	}

	public ReglasEspeciales getReglas() {
		return reglas;
	}

	public int getMaxParticipantes() {
		return maxParticipantes;
	}
	
	
}
