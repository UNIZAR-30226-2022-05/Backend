package es.unizar.pruebaCliente;

import com.google.gson.Gson;

public class Serializar {
	public static <T> T deserializar(String jsonData, Class<T> expectedClass){
        return new Gson().fromJson(jsonData, expectedClass);
    }
	
	public static String serializar(Object objeto){
        return new Gson().toJson(objeto);
    }
}
