package es.unizar.unoforall.db;

import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GestorPoolConexionesBD {
        private static final int MAX_CONEXIONES = 20;
        
        private static BlockingQueue<Connection> conexiones;

	public static void inicializarPool(){
            System.out.println("Inicializando pool de conexiones de PostgreSQL...");
	    conexiones = new ArrayBlockingQueue(MAX_CONEXIONES);
	    for(int i=0;i<MAX_CONEXIONES;i++){
	        try{
	            Connection conexion = GestorConexionesBD.getConnection();
	            conexiones.add(conexion);
	        }catch(Exception ex){
	            System.err.println("Error al inicializar el PoolConnectionManager - Sugerencia: sudo service postgresql start");
                    System.exit(1);
                    break;
	            //ex.printStackTrace();
	        }
	    }
            System.out.println("Pool de conexiones inicializado");
	}

	public static Connection getConnection(){
            Connection conexion = null;
            
            // Se bloquea hasta que consigue una conexión
            while(conexion == null){
                try{
                    conexion = conexiones.take();
                }catch(InterruptedException ex){}
            }
            
            return conexion;
	}

	public static void releaseConnection(Connection conexion){
            conexiones.add(conexion);
	}

	public static void close(){
            conexiones.forEach(conexion -> GestorConexionesBD.releaseConnection(conexion));
            conexiones.clear();
	}
}
