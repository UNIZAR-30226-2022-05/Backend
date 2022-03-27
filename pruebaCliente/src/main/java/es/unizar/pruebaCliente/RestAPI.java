package es.unizar.pruebaCliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;

public class RestAPI{
    private static final String SERVER_IP = "http://localhost";
    private static final int HTTP_OK = 200;
    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private final Map<String, String> parameters;
    private String fullIP;
    private HttpURLConnection conexion;
    private boolean closed;
    private Consumer<Exception> onError = ex -> {ex.printStackTrace(); close();};

    private Gson gson = null;
    private Gson getGson(){
        if(gson == null){
            gson = new Gson();
        }
        return gson;
    }

    public RestAPI(String seccion){
        parameters = new HashMap<>();
        fullIP = SERVER_IP + seccion;
        conexion = null;
        closed = false;
    }

    public void setOnError(Consumer<Exception> onError){
        this.onError = onError;
    }

    public <T> void addParameter(String key, T value){
    	if (value instanceof String) {
    		parameters.put(key, (String)value);
    	} else {
    		parameters.put(key, getGson().toJson(value));
    	}
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
            //System.out.println(data + "holi");
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
        }catch(IOException ex){
            onError.accept(ex);
        }
    }

    public <T> T receiveObject(Class<T> requestedClass){
        if(closed){
            return null;
        }
        try {
            InputStream responseBody = conexion.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
            return getGson().fromJson(responseBodyReader, requestedClass);
        }catch(IOException ex){
            onError.accept(ex);
            return null;
        }
    }
    
    public <T> T receiveObject(Type requestedClass){
        if(closed){
            return null;
        }
        try {
            InputStream responseBody = conexion.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
            return getGson().fromJson(responseBodyReader, requestedClass);
        }catch(IOException ex){
            onError.accept(ex);
            return null;
        }
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

        gson = null;
        closed = true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
