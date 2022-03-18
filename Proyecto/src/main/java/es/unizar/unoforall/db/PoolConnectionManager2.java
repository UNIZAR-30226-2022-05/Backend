package es.unizar.unoforall.db;

import java.sql.Connection;
import java.sql.SQLException;

public class PoolConnectionManager2 {
	private static Connection[] conexiones = new Connection[20];
	
	static {
		for(int i = 0; i < 20; i++) {
			try {
				conexiones[i] = ConnectionManager.getConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//public static synchronized Connection getConnection()
	
}
