package es.unizar.unoforall.model.partidas;

import java.util.UUID;

public class EnvioEmoji {
	int emoji;
	UUID emisor;
	boolean esIA;
	
	public EnvioEmoji(int emoji, UUID emisor, boolean esIA) {
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
	public UUID getEmisor() {
		return emisor;
	}
	public void setEmisor(UUID emisor) {
		this.emisor = emisor;
	}

	public boolean isEsIA() {
		return esIA;
	}

	public void setEsIA(boolean esIA) {
		this.esIA = esIA;
	}
	
	
}
