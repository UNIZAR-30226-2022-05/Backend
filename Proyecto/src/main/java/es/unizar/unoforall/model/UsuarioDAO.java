package es.unizar.unoforall.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import es.unizar.unoforall.db.ConnectionManager;
import es.unizar.unoforall.db.PoolConnectionManager;

public class UsuarioDAO {
/******************************* Gestión de usuarios *******************************/
    
	/**
	 * Método para registrar un usuario
	 * @param usuario
	 * @return Si el usuario se ha registrado correctamente
	 */
	public static boolean registrarUsuario(UsuarioVO usuario) {
		boolean result = false;
		Connection conn = null;
		
		try {
			conn = PoolConnectionManager.getConnection();
			
			UUID idUsuario = usuario.getId();
			String correo = usuario.getCorreo();
			String nombre = usuario.getNombre();
			String password = usuario.getContrasenna();
			int puntos = usuario.getPuntos();
			int totalPartidas = usuario.getTotalPartidas();
			int numVictorias = usuario.getNumVictorias();
			PreparedStatement addUser = conn.prepareStatement("INSERT INTO usuarios VALUES(?, ?, ?, ?, ?, ?, ?)"
															+ "ON CONFLICT(id) DO UPDATE "
															+ "SET	correo=EXCLUDED.correo,"
															+ "		nombre=EXCLUDED.nombre,"
															+ "		contrasenna=EXCLUDED.contrasenna,"
															+ "		puntos=EXCLUDED.puntos,"
															+ "		total_partidas=EXCLUDED.total_partidas,"
															+ "		num_victorias=EXCLUDED.num_victorias;");
			addUser.setObject(1, idUsuario);
			addUser.setString(2, correo);
			addUser.setString(3, nombre);
			addUser.setString(4, password);
			addUser.setInt(5, puntos);
			addUser.setInt(6, totalPartidas);
			addUser.setInt(7, numVictorias);
			addUser.execute();
			
			result = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		return result;
	}
	
	/**
	 * Método para modificar un usuario
	 * @param usuario
	 * @return Si el usuario se ha modificado correctamente
	 */
	public static boolean modificarUsuario(UsuarioVO usuario) {
		boolean result = false;
		Connection conn = null;
		
		try {
			conn = PoolConnectionManager.getConnection();
			
			UUID idUsuario = usuario.getId();
			String correo = usuario.getCorreo();
			String nombre = usuario.getNombre();
			String password = usuario.getContrasenna();
			int puntos = usuario.getPuntos();
			int totalPartidas = usuario.getTotalPartidas();
			int numVictorias = usuario.getNumVictorias();
			
			PreparedStatement updateUser = conn.prepareStatement("UPDATE usuarios SET "
																	+ "nombre=?, "
																	+ "correo=?, "
																	+ "contrasenna=?, "
																	+ "puntos=?, "
																	+ "total_partidas=? "
																	+ "num_victorias=? "
																	+ "WHERE id=?;");
			
			updateUser.setString(1, nombre);
			updateUser.setString(2, correo);
			updateUser.setString(3, password);
			updateUser.setInt(4, puntos);
			updateUser.setInt(5, totalPartidas);
			updateUser.setInt(6, numVictorias);
			updateUser.setObject(7, idUsuario);
			updateUser.execute();
			
			result = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		
		return result;
	}
	
	/**
	 * Método para que un usuario inicie sesión
	 * @param correo
	 * @param password
	 * @return El usuario correspondiente o null si ha habido algún error
	 */
	public static UsuarioVO loguearUsuario(String correo, String password) {
		UsuarioVO user = getUsuario(correo);
		if(user != null && user.getContrasenna().equals(password)) {
			return user;
		}else {
			return null;
		}
	}
	
	/**
	 * Método para obtener un usuario por su ID
	 * @param idUsuario
	 * @return El usuario correspondiente o null si no existe
	 */
	public static UsuarioVO getUsuario(UUID idUsuario) {
		UsuarioVO result = null;
		Connection conn = null;
		
		try {
			conn = PoolConnectionManager.getConnection();
			
			PreparedStatement selectUser = conn.prepareStatement("SELECT * FROM usuarios WHERE id = ?;");
			selectUser.setObject(1, idUsuario);
			
			ResultSet rs = selectUser.executeQuery();
			if(rs.next()) {
				String correo = rs.getString("correo");
				String nombre = rs.getString("nombre");
				String password = rs.getString("contrasenna");
				int puntos = rs.getInt("puntos");
				int totalPartidas = rs.getInt("total_partidas");
				int numVictorias = rs.getInt("num_victorias");
				
				result = new UsuarioVO(idUsuario, correo, nombre, password, puntos, totalPartidas, numVictorias);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		return result;
	}
	
	/**
	 * Método para obtener un usuario por su correo
	 * @param correo
	 * @return El usuario correspondiente o null si no existe
	 */
	public static UsuarioVO getUsuario(String correo) {
		UsuarioVO result = null;
		Connection conn = null;
		
		try {
			conn = PoolConnectionManager.getConnection();
			
			PreparedStatement selectUser = conn.prepareStatement("SELECT * FROM usuarios WHERE correo = ?;");
			selectUser.setString(1, correo);
			
			ResultSet rs = selectUser.executeQuery();
			if(rs.next()) {
				UUID idUsuario = (UUID) rs.getObject("id");
				String nombre = rs.getString("nombre");
				String password = rs.getString("contrasenna");
				int puntos = rs.getInt("puntos");
				int totalPartidas = rs.getInt("total_partidas");
				int numVictorias = rs.getInt("num_victorias");
				
				result = new UsuarioVO(idUsuario, correo, nombre, password, puntos, totalPartidas, numVictorias);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		return result;
	}
	
	
	/**
	 * Método para eliminar un usuario
	 * @param usuario
	 * @return Si el usuario se ha eliminado correctamente
	 */
	public static boolean eliminarUsuario(UsuarioVO usuario) {
		boolean result = false;
		Connection conn = null;
		
		try {
			conn = PoolConnectionManager.getConnection();
			
			UUID idUsuario = usuario.getId();
			PreparedStatement delUser = conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario=?;");
			delUser.setObject(1, idUsuario);
			delUser.execute();
			
			result = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		
		return result;
	}
	
	
}
