package es.unizar.unoforall.model;


import java.util.UUID;

/**
 * Clase que representa un usuario
 * 
 */
public class UsuarioVO {	
	private final UUID id;
	private String correo;
	private String nombre;
	private String contrasenna;
	private int puntos;
	private int totalPartidas;
	private int numVictorias;
	
	
	public UsuarioVO(UUID id, String correo, String nombre, String contrasenna, int puntos, int totalPartidas, int numVictorias) {
		if(id == null) {
			id = UUID.randomUUID();
		}
		this.id = id;
		this.correo = correo;
		this.nombre = nombre;
		this.contrasenna = contrasenna;
		this.puntos = puntos;
		this.totalPartidas = totalPartidas;
		this.numVictorias = numVictorias;
	}
	
	public UsuarioVO(String correo, String nombre, String contrasenna) {
		this.id = UUID.randomUUID();
		this.correo = correo;
		this.nombre = nombre;
		this.contrasenna = contrasenna;
		this.puntos = 0;
		this.totalPartidas = 0;
		this.numVictorias = 0;
	}


	public String getCorreo() {
		return correo;
	}


	public void setCorreo(String correo) {
		this.correo = correo;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getContrasenna() {
		return contrasenna;
	}


	public void setContrasenna(String contrasenna) {
		this.contrasenna = contrasenna;
	}


	public int getPuntos() {
		return puntos;
	}


	public void setPuntos(int puntos) {
		this.puntos = puntos;
	}


	public UUID getId() {
		return id;
	}


	public int getTotalPartidas() {
		return totalPartidas;
	}


	public void setTotalPartidas(int totalPartidas) {
		this.totalPartidas = totalPartidas;
	}


	public int getNumVictorias() {
		return numVictorias;
	}


	public void setNumVictorias(int numVictorias) {
		this.numVictorias = numVictorias;
	}
}
