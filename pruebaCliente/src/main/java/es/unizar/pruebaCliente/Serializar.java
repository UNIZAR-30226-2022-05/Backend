package es.unizar.pruebaCliente;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class Serializar {
    private static final boolean DEBUG = false;

    public static <T> String serializar(T dato){
        if(dato instanceof String){
            return (String) dato;
        }else{
            return new Gson().toJson(dato);
        }
    }

    public static <T> T deserializar(String mensaje, Type expectedClass){
        if(DEBUG){
            System.out.println("Mensaje recibido: " + mensaje);
        }
        
        if(mensaje.equals("null") || mensaje.equals("nulo")){
            return (T) null;
        }
        
        if(expectedClass.equals(String.class)){
            return (T) mensaje;
        }else{
            return new Gson().fromJson(mensaje, expectedClass);
        }
    }

    public static <T> T deserializar(InputStream inputStream, Class<T> expectedClass) throws IOException {
        StringBuilder mensajeBuilder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesReaded;
        while((bytesReaded = inputStream.read(buffer)) > 0){
            mensajeBuilder.append(new String(buffer, 0, bytesReaded));
        }

        String mensaje = mensajeBuilder.toString();
        return deserializar(mensaje, expectedClass);
    }
}
