package es.unizar.unoforall.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class GestorPoolConexionesBD {
	private static Object LOCK;
	private static Map<Connection, Boolean> conexiones;
	private static final int MAX_CONEXIONES = 20;

	public static void inicializarPool(){
	    LOCK = new Object();

	    conexiones = new HashMap<>();
	    for(int i=0;i<MAX_CONEXIONES;i++){
	        try{
	            Connection conexion = GestorConexionesBD.getConnection();
	            conexiones.put(conexion, true);
	        }catch(Exception ex){
	            System.err.println("Error al inicializar el PoolConnectionManager - Sugerencia: sudo service postgresql start");
	            //ex.printStackTrace();
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
