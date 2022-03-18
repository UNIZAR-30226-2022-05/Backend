package es.unizar.unoforall.db;

import java.sql.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/** * Clasequeabstraelaconexionconla base dedatos. */

public class PoolConnectionManager {
	// Devuelvela conexion, para no tener que abrirla y cerrarla siempre.
	public final static Connection getConnection() {
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");

			System.out.println(envCtx.toString());
			DataSource ds = (DataSource) envCtx.lookup("jdbc/accessDB");
			System.out.println(ds.toString());

			Connection conn = ds.getConnection();
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Libera la conexion, devolviendola al pool
	public final static void releaseConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
