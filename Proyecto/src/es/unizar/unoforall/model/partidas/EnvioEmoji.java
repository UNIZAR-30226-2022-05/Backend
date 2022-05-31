package es.unizar.unoforall.model.partidas;


public class EnvioEmoji {
	int emoji;
	int emisor;
	boolean esIA;
	
	public EnvioEmoji(int emoji, int emisor, boolean esIA) {
		super();
		this.emoji = emoji;
		this.emisor = emisor;
		this.esIA = esIA;
	}
	
	public int getEmoji() {
		return emoji;
	}
	public void setEmoji(int emoji) {
		this.emoji = emoji;
	}
	public int getEmisor() {
		return emisor;
	}
	public void setEmisor(int emisor) {
		this.emisor = emisor;
	}

	public boolean isEsIA() {
		return esIA;
	}

	public void setEsIA(boolean esIA) {
		this.esIA = esIA;
	}
	
	
}
