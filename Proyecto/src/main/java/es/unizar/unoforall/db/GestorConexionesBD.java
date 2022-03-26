package es.unizar.unoforall.db;


import java.sql.*;

/**
 * Clase que abstrae la conexion con la base de datos.
 * 
 */
public class GestorConexionesBD {
	// JDBC nombred el driver y URL de BD 
	private static final String JDBC_DRIVER = "org.postgresql.Driver";
	private static final String DB_URL = "jdbc:postgresql://localhost:5432/uno_for_all_db?currentSchema=uno_for_all";
	
	// Credenciales de la Base de Datos
	private static final String USER = "postgres";
	private static final String PASS = "unoforall";
	
	/**
	 * Devuelve una nueva conexión a la base de datos
	 * @return La nueva conexión
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public final static Connection getConnection() throws SQLException, ClassNotFoundException {
		Connection conn = null;

		//STEP 1: Register JDBC driver
		Class.forName(JDBC_DRIVER);
		//STEP 2: Open a connection
		//System.out.println("Connecting to database...");
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		return conn; 
	} 
	
	/**
	 * Libera una conexión abierta
	 * @param conn
	 */
	public final static void releaseConnection(Connection conn){
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}

