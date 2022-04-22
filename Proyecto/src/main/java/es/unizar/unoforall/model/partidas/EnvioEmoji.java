package es.unizar.unoforall.model.partidas;

import java.util.UUID;

public class EnvioEmoji {
	int emoji;
	UUID emisor;
	
	public EnvioEmoji(int emoji, UUID emisor) {
		super();
		this.emoji = emoji;
		this.emisor = emisor;
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
	
	
}
