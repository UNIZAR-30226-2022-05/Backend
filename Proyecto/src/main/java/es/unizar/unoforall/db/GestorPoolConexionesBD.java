package es.unizar.unoforall.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GestorPoolConexionesBD {
	private static final Object LOCK;
	private static final Map<Connection, Boolean> conexiones;
	private static final int MAX_CONEXIONES = 20;

	static{
	    LOCK = new Object();

	    conexiones = new HashMap<>();
	    for(int i=0;i<MAX_CONEXIONES;i++){
	        try{
	            Connection conexion = GestorConexionesBD.getConnection();
	            conexiones.put(conexion, true);
	        }catch(Exception ex){
	            System.err.println("Error al inicializar el PoolConnectionManager:");
	            ex.printStackTrace();
	        }
	    }
	}

	public static Connection getConnection(){
	    synchronized(LOCK){
	        Connection result = null;
	        while(result == null){
	            for(Map.Entry<Connection, Boolean> entry : conexiones.entrySet()){
	                Connection conexion = entry.getKey();
	                boolean disponible = entry.getValue();
	                if(disponible){
	                    result = conexion;
	                    conexiones.put(conexion, false);
	                    break;
	                }
	            }

	            if(result == null){
	                try{
	                    LOCK.wait();
	                }catch(Exception ex){}
	            }
	        }

	        return result;
	    }
	}

	public static void releaseConnection(Connection conexion){
	    synchronized(LOCK){
	        if(conexiones.containsKey(conexion)){
	            conexiones.put(conexion, true);
	            try{
	                LOCK.notify();
	            }catch(Exception ex){}
	        }
	    }
	}

	public static void close(){
	    synchronized(LOCK){
	        conexiones.forEach((conexion, disponible) -> GestorConexionesBD.releaseConnection(conexion));
	    }
	}
}
