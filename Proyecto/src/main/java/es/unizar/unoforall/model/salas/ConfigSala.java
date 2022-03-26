package es.unizar.unoforall.model.salas;


public class ConfigSala {
	public enum ModoJuego{Original, Attack, Parejas};
	
	private ModoJuego modoJuego;
	private ReglasEspeciales reglas;
	private int maxParticipantes;
	private boolean esPublica;
	
	public ConfigSala(ModoJuego modoJuego, ReglasEspeciales reglas, 
			int maxParticipantes, boolean esPublica) {
		super();
		this.modoJuego = modoJuego;
		this.reglas = reglas;
		this.maxParticipantes = maxParticipantes;
		this.esPublica = esPublica;
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

	public boolean isEsPublica() {
		return esPublica;
	}
}
