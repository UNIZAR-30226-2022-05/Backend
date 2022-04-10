package es.unizar.unoforall.model.partidas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.unizar.unoforall.model.salas.ConfigSala;

public class Partida {
	private List<Carta> mazo;
	private List<Carta> cartasJugadas;
	
	private List<Jugador> jugadores;
	private int turno;
	private boolean sentidoHorario;
	
	private ConfigSala configuracion;
	private boolean terminada;	// si está a true, ha ganado el jugador al 
									// apunta 'turno'
		
	public Partida(List<UUID> jugadoresID, int numIAs, ConfigSala configuracion) {
		//Mazo
		this.mazo = new LinkedList<>();
		for(Carta.Color color : Carta.Color.values()) {
			if (color != Carta.Color.comodin) {
				for(Carta.Tipo tipo : Carta.Tipo.values()) {
					if (tipo == Carta.Tipo.n0) {
						this.mazo.add(new Carta(tipo,color));
					} else {	//dos veces
						this.mazo.add(new Carta(tipo,color));
						this.mazo.add(new Carta(tipo,color));
					}
				}
			} else {
				for(int i = 0; i < 4; i++) {
					this.mazo.add(new Carta(Carta.Tipo.cambioColor,Carta.Color.comodin));
					this.mazo.add(new Carta(Carta.Tipo.mas4,Carta.Color.comodin));
				}
			}
		}
		Collections.shuffle(this.mazo);
		
		
		// Cartas jugadas
		this.cartasJugadas = new ArrayList<>();
		
		
		// Jugadores
		this.jugadores = new LinkedList<>();
		for(UUID jID : jugadoresID) {
			this.jugadores.add(new Jugador(jID));
		}
			// Se crean las IA
		for(int i = 0; i < numIAs; i++) {
			this.jugadores.add(new Jugador());
		}
			// Se crean las manos de todos los jugadores
		for(Jugador j : this.jugadores) {
			for (int i = 0; i < 7; i++) {
				j.getMano().add(this.mazo.get(0));
			}
		}
		
		
		// Resto
		this.turno = 0;
		this.sentidoHorario = true;
		this.configuracion = configuracion;
		this.terminada = false;
	}


	// Introduce todas las cartas jugadas salvo la última debajo del mazo y 
	// barajeadas
	private void agnadirCartasMazo() {
		
	}
	
	
	//requiere un objeto Jugada
	public void ejecutarJugada() {
		//comprobar validez (jugador correcto, carta correcta); si no se ignora
		//ejecutar
		//ver si ha ganado
		
		//eventos asíncronos: la carta rayosX, emojis, botón de UNO, tiempo, votación pausa
	}
	
	public void ejecutarJugadaIA() {
		
	}
	
	public void expulsarJugador(UUID jugador) {
		//se sustituye por IA
	}
	
	
	/**************************************************************************/
	// Para los FRONTENDs
	/**************************************************************************/
	
	public List<Jugador> getJugadores() {
		return jugadores;
	}
	
	public UUID getIDJugadorActual() {
		return this.jugadores.get(this.turno).getJugadorID();
	}
	
	public Carta getUltimaCartaJugada() {
		return this.cartasJugadas.get(0);
	}
	
	// Se debe mirar en cada turno, y cuando devuelva true ya se puede desconectar
	// del buzón de la partida con websockets
	public boolean estaTerminada() {
		return this.terminada;
	}
}
