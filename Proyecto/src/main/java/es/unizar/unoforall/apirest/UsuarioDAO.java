package es.unizar.unoforall.apirest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.model.UsuarioVO;

public class UsuarioDAO {
/**************************** Gestión de usuarios *****************************/
    
	/**
	 * Método para registrar un usuario
	 * @param usuario
	 * @return Si el usuario se ha registrado correctamente
	 */
	public static boolean registrarUsuario(UsuarioVO usuario) {
		boolean result = false;
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			
			UUID idUsuario = usuario.getId();
			String correo = usuario.getCorreo();
			String nombre = usuario.getNombre();
			String password = usuario.getContrasenna();
			int puntos = usuario.getPuntos();
			int totalPartidas = usuario.getTotalPartidas();
			int numVictorias = usuario.getNumVictorias();
			PreparedStatement addUser = 
					conn.prepareStatement("INSERT INTO usuarios VALUES(?, ?, ?, ?, ?, ?, ?)"
											+ "ON CONFLICT(id) DO UPDATE "
											+ "SET	correo=EXCLUDED.correo,"
											+ "		contrasenna=EXCLUDED.contrasenna,"
											+ "		nombre=EXCLUDED.nombre,"
											+ "		puntos=EXCLUDED.puntos,"
											+ "		total_partidas=EXCLUDED.total_partidas,"
											+ "		num_victorias=EXCLUDED.num_victorias;");
			addUser.setObject(1, idUsuario);
			addUser.setString(2, correo);
			addUser.setString(3, password);
			addUser.setString(4, nombre);
			addUser.setInt(5, puntos);
			addUser.setInt(6, totalPartidas);
			addUser.setInt(7, numVictorias);
			addUser.execute();
			
			result = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
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
			conn = GestorPoolConexionesBD.getConnection();
			
			UUID idUsuario = usuario.getId();
			String correo = usuario.getCorreo();
			String nombre = usuario.getNombre();
			String password = usuario.getContrasenna();
			int puntos = usuario.getPuntos();
			int totalPartidas = usuario.getTotalPartidas();
			int numVictorias = usuario.getNumVictorias();
			
			PreparedStatement updateUser = 
					conn.prepareStatement("UPDATE usuarios SET "
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
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		
		return result;
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
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement selectUser = 
					conn.prepareStatement("SELECT * FROM usuarios WHERE id = ?;");
			selectUser.setObject(1, idUsuario);
			
			ResultSet rs = selectUser.executeQuery();
			if(rs.next()) {
				String correo = rs.getString("correo");
				String nombre = rs.getString("nombre");
				String password = rs.getString("contrasenna");
				int puntos = rs.getInt("puntos");
				int totalPartidas = rs.getInt("total_partidas");
				int numVictorias = rs.getInt("num_victorias");
				
				result = new UsuarioVO(idUsuario, correo, nombre, 
						password, puntos, totalPartidas, numVictorias);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		return result;
	}
	
	/**
	 * Método para actualizar la contraseña de un usuario dado su identificador
	 * @param idUsuario contiene el identificador de la cuenta en la base de datos.
	 * @param contrasenna contiene la nueva contraseña
	 * @return Un String que contiene el error en caso de error, y null si hay éxito.
	 */
	public static String cambiarContrasenna(UUID idUsuario, String contrasenna) {
		String result = "null";
		Connection conn = null;
		try {
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement updateUser = 
					conn.prepareStatement("UPDATE usuarios SET contrasenna = ? WHERE id = ?;");
			updateUser.setString(1, contrasenna);
			updateUser.setObject(2, idUsuario);
			
			int rows = updateUser.executeUpdate();
			if(rows != 1) {
				result = "Ha habido un error con la actualización de la cuenta. Cuentas modificadas: " + Integer.toString(rows)+".";
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		return result;
	}
	
	/**
	 * Método para actualizar el nombre, contraseña y correo de un usuario dado su antiguo correo.
	 * @param correoNuevo contiene el nuevo correo para la cuenta.
	 * @param usuario contiene los nuevos valores de nombre y contraseña. Además del antiguo
	 * 					  correo de la cuenta.
	 * @return null si tiene éxito, o un String en caso de error.
	 */
	public static String actualizarCuenta(UsuarioVO usuario) {
		String result = "null";
		Connection conn = null;
		try {
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement updateUser = 
					conn.prepareStatement("UPDATE usuarios SET correo = ?, nombre = ?, contrasenna = ? WHERE id = ?;");
			updateUser.setString(1, usuario.getCorreo());
			updateUser.setString(2, usuario.getNombre());
			updateUser.setString(3, usuario.getContrasenna());
			updateUser.setObject(4, usuario.getId());
			
			int rows = updateUser.executeUpdate();
			if(rows != 1) {
				result = "Ha habido un error con la actualización de la cuenta. Cuentas modificadas: " + Integer.toString(rows)+".";
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
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
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement selectUser = 
					conn.prepareStatement("SELECT * FROM usuarios WHERE correo = ?;");
			selectUser.setString(1, correo);
			
			ResultSet rs = selectUser.executeQuery();
			if(rs.next()) {
				UUID idUsuario = (UUID) rs.getObject("id");
				String nombre = rs.getString("nombre");
				String password = rs.getString("contrasenna");
				int puntos = rs.getInt("puntos");
				int totalPartidas = rs.getInt("total_partidas");
				int numVictorias = rs.getInt("num_victorias");
				
				result = new UsuarioVO(idUsuario, correo, nombre, password, 
						puntos, totalPartidas, numVictorias);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
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
			conn = GestorPoolConexionesBD.getConnection();
			
			UUID idUsuario = usuario.getId();
			PreparedStatement delUser = 
					conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario=?;");
			delUser.setObject(1, idUsuario);
			delUser.execute();
			
			result = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		
		return result;
	}
	
	/**
	 * Dado el id del usuario, devuelve la lista de usuarios a los que ha solicitado amistad y que
	 * no le han aceptado ni rechazado.
	 * @param idUsuario	contiene el id de la cuenta del usuario
	 * @return			devuelve una lista de usuarios indicando que la sesión no ha expirado y una lista con
	 * 					los usuarios que ha sacado. Si ha habido un error, lo ha indicado en el atributo <error>
	 * 					de la lista devuelta.
	 */
	public static ListaUsuarios sacarPeticionesEnviadas(UUID idUsuario) {
		ListaUsuarios lu = new ListaUsuarios(false);
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			PreparedStatement getRequests = 
					conn.prepareStatement("SELECT receptor FROM amigo_de WHERE emisor = ? and aceptada=false");
			getRequests.setObject(1,idUsuario);
			ResultSet rs = getRequests.executeQuery();
			while(rs.next()) {
				UsuarioVO user = getUsuario((UUID) rs.getObject("receptor"));
				user.setContrasenna(null); //No pasamos datos confidenciales
				lu.getUsuarios().add(user);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			lu.setError("Ha surgido un problema con la base de datos.");
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		return lu;
	}
	
	/**
	 * Dado el id del usuario, devuelve la lista de usuarios que le han solicitado amistad y que no
	 * les ha dado respuesta aún.
	 * @param idUsuario	contiene el id de la cuenta del usuario
	 * @return			devuelve una lista de usuarios indicando que la sesión no ha expirado y una lista con
	 * 					los usuarios que ha sacado. Si ha habido un error, lo ha indicado en el atributo <error>
	 * 					de la lista devuelta.
	 */
	public static ListaUsuarios sacarPeticionesRecibidas(UUID idUsuario) {
		ListaUsuarios lu = new ListaUsuarios(false);
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			PreparedStatement getRequests = 
					conn.prepareStatement("SELECT emisor FROM amigo_de WHERE receptor = ? and aceptada=false");
			getRequests.setObject(1,idUsuario);
			ResultSet rs = getRequests.executeQuery();
			while(rs.next()) {
				UsuarioVO user = getUsuario((UUID) rs.getObject("emisor"));
				user.setContrasenna(null); //No pasamos datos confidenciales
				lu.getUsuarios().add(user);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			lu.setError("Ha surgido un problema con la base de datos.");
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		return lu;
	}
	
	/**
	 * Dado el id del usuario, devuelve la lista de usuarios a los que ha aceptado la solicitud de amistad.
	 * @param idUsuario	contiene el id de la cuenta del usuario
	 * @return			devuelve una lista de usuarios indicando que la sesión no ha expirado y una lista con
	 * 					los usuarios que ha sacado. Si ha habido un error, lo ha indicado en el atributo <error>
	 * 					de la lista devuelta.
	 */
	public static ListaUsuarios sacarAmigos(UUID idUsuario) {
		ListaUsuarios lu = new ListaUsuarios(false);
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			//Sacar los amigos que me solicitaron.
			PreparedStatement getRequestsE = 
					conn.prepareStatement("SELECT emisor FROM amigo_de WHERE receptor = ? and aceptada=true");
			getRequestsE.setObject(1,idUsuario);
			ResultSet rsE = getRequestsE.executeQuery();
			
			while(rsE.next()) {
				UsuarioVO user = getUsuario((UUID) rsE.getObject("emisor"));
				user.setContrasenna(null); //No pasamos datos confidenciales
				lu.getUsuarios().add(user);
			}
			
			//Sacar los amigos que solicité.
			PreparedStatement getRequestsR = 
					conn.prepareStatement("SELECT receptor FROM amigo_de WHERE emisor = ? and aceptada=true");
			getRequestsR.setObject(1,idUsuario);
			ResultSet rsR = getRequestsR.executeQuery();
			
			while(rsR.next()) {
				UsuarioVO user = getUsuario((UUID) rsR.getObject("receptor"));
				user.setContrasenna(null); //No pasamos datos confidenciales
				lu.getUsuarios().add(user);
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
			lu.setError("Ha surgido un problema con la base de datos.");
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		return lu;
	}
	
	/**
	 * Dado el id del usuario, devuelve la lista de usuarios a los que ha aceptado la solicitud de amistad.
	 * @param idUsuario	contiene el id de la cuenta del usuario
	 * @return			devuelve null si todo va bien. En caso contrario devuelve un mensaje de error.
	 */
	public static String mandarPeticion(UUID idUsuario, UUID amigo) {
		String error = "null";
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			//Sacar los amigos que me solicitaron.
			PreparedStatement getRequest = 
					conn.prepareStatement("SELECT * FROM amigo_de WHERE emisor = ? and receptor = ? and aceptada=false");
			getRequest.setObject(1,amigo);
			getRequest.setObject(2, idUsuario);
			ResultSet rs = getRequest.executeQuery();
			
			if(rs.next()) { //Ya había una petición de amistado
				PreparedStatement updateRequest = 
						conn.prepareStatement("UPDATE amigo_de SET aceptada = true WHERE emisor = ? and receptor = ?;");
				updateRequest.setObject(1,amigo);
				updateRequest.setObject(2, idUsuario);
				int rows = updateRequest.executeUpdate();
				if(rows != 1) {
					error = "Ha habido un error con la solicitud de amistad. Solicitudes acpetadas: " + Integer.toString(rows)+".";
				}
			} else { //Se crea la solicitud de amistad
				PreparedStatement insertRequest = conn.prepareStatement("INSERT INTO amigo_de VALUES(?,?,false)");
				insertRequest.setObject(1, idUsuario);
				insertRequest.setObject(2,amigo);
				insertRequest.execute();
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
			error = "Ha surgido un problema con la base de datos.";
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		return error;
	}
}
