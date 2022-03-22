package es.unizar.unoforall.utils;

import com.google.gson.Gson;

public class Deserializar {
	public static <T> T deserializar(String jsonData, Class<T> expectedClass){
        return new Gson().fromJson(jsonData, expectedClass);
    }
}
