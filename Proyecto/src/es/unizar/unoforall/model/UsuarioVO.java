package es.unizar.unoforall.model;


import java.util.UUID;

/**
 * Clase que representa un usuario
 * 
 */
public class UsuarioVO {	
	private boolean exito;
	
	private UUID id;
	private String correo;
	private String nombre;
	private String contrasenna;
	private int puntos;
	private int totalPartidas;
	private int numVictorias;
	private int avatar;
	private int aspectoTablero;
	private int aspectoCartas;
	
	

	public UsuarioVO() {
		exito = false;
	}
	
	public UsuarioVO(UUID id, String correo, String nombre, String contrasenna, 
			int puntos, int totalPartidas, int numVictorias, int avatar,
			int aspectoTablero, int aspectoCartas) {
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
		this.exito = true;
		this.avatar = avatar;
		this.aspectoTablero = aspectoTablero;
		this.aspectoCartas = aspectoCartas;
	}
	
	public UsuarioVO(UUID id, String correo, String nombre, String contrasenna) {
		if(id == null) {
			id = UUID.randomUUID();
		}
		this.id = id;
		this.correo = correo;
		this.nombre = nombre;
		this.contrasenna = contrasenna;
		this.puntos = 0;
		this.totalPartidas = 0;
		this.numVictorias = 0;
		this.exito = true;
		this.avatar = 0;
		this.aspectoTablero = 0;
		this.aspectoCartas = 0;
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

	@Override
	public String toString() {
		return "UsuarioVO [id=" + id + ", correo=" + correo + ", nombre=" + nombre + ", contrasenna=" + contrasenna
				+ ", puntos=" + puntos + ", totalPartidas=" + totalPartidas + ", numVictorias=" + numVictorias + "]";
	}

	public boolean isExito() {
		return exito;
	}
	
	public int getAvatar() {
		return avatar;
	}

	public void setAvatar(int avatar) {
		switch(avatar) {
			case 0:
				this.avatar = avatar;
				break;
			case 1:
				if (this.puntos >= 10) {
					this.avatar = avatar;
				}
				break;
			case 2:
				if (this.puntos >= 30) {
					this.avatar = avatar;
				}
				break;
			case 3:
				if (this.puntos >= 50) {
					this.avatar = avatar;
				}
				break;
			case 4:
				if (this.puntos >= 100) {
					this.avatar = avatar;
				}
				break;
			case 5:
				if (this.puntos >= 200) {
					this.avatar = avatar;
				}
				break;
			case 6:
				if (this.puntos >= 500) {
					this.avatar = avatar;
				}
				break;
			default:	//avatar inválido
				break;
		}
	}

	public int getAspectoTablero() {
		return aspectoTablero;
	}

	public void setAspectoTablero(int aspectoTablero) {
		this.aspectoTablero = aspectoTablero;
	}

	public int getAspectoCartas() {
		return aspectoCartas;
	}

	public void setAspectoCartas(int aspectoCartas) {
		this.aspectoCartas = aspectoCartas;
	}
}
