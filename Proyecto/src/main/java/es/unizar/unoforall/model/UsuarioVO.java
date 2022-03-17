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
	private boolean puntos;
	
	
	public UsuarioVO(UUID id, String correo, String nombre, String contrasenna, boolean puntos) {
		super();
		if(id == null) {
			id = UUID.randomUUID();
		}
		this.id = id;
		this.correo = correo;
		this.nombre = nombre;
		this.contrasenna = contrasenna;
		this.puntos = puntos;
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


	public boolean isPuntos() {
		return puntos;
	}


	public void setPuntos(boolean puntos) {
		this.puntos = puntos;
	}


	public UUID getId() {
		return id;
	}	
	
	
}
