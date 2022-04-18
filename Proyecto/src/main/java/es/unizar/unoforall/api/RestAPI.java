package es.unizar.unoforall.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RestAPI{
    private static String SERVER_URL = "http://localhost";
    private static final int HTTP_OK = 200;
    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private final Map<String, String> parameters;
    private String fullIP;
    private HttpURLConnection conexion;
    private boolean closed;
    private Consumer<Exception> onError = ex -> {ex.printStackTrace(); close();};
    
    public static void setServerIP(String serverIP){
        RestAPI.SERVER_URL = "http://" + serverIP;
    }

    public RestAPI(String seccion){
        parameters = new HashMap<>();
        fullIP = SERVER_URL + seccion;
        conexion = null;
        closed = false;
    }

    public void setOnError(Consumer<Exception> onError){
        this.onError = onError;
    }

    public <T> void addParameter(String key, T value){
    	parameters.put(key, Serializar.serializar(value));
    }

    private static String getDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    public void openConnection(){
        if(closed){
            return;
        }
        try{
            String data = getDataString(parameters);
            
            URL url = new URL(fullIP);
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod( "POST" );
            conexion.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            conexion.setDoOutput(true);

            conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream output = conexion.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
            writer.write(data);
            writer.flush();

            if(conexion.getResponseCode() != HTTP_OK){
                conexion.disconnect();
                throw new IOException(String.format("Obtained response code %d while connecting to %s",
                        conexion.getResponseCode(),
                        fullIP));
            }
        }catch(Exception ex){
            onError.accept(ex);
        }
    }
    
    public <T> T receiveObject(Class<T> requestedClass, boolean autoClose){
        if(closed){
            return null;
        }
        try {
            InputStream responseBody = conexion.getInputStream();
            T dato = Serializar.deserializar(responseBody, requestedClass);
            if(autoClose){
                close();
            }
            
            return dato;
        }catch(Exception ex){
            onError.accept(ex);
            return null;
        }
    }
    public <T> T receiveObject(Class<T> requestedClass){
        return receiveObject(requestedClass, true);
    }

    public void close(){
        if(closed){
            return;
        }

        try{
            conexion.getInputStream().close();
        }catch(Exception ex){}
        try{
            conexion.getOutputStream().close();
        }catch(Exception ex){}
        try{
            conexion.disconnect();
        }catch(Exception ex){}
        
        closed = true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
