package es.unizar.pruebaCliente.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    //Fuente: https://www.baeldung.com/sha-256-hashing-java
    //          consultado por última vez el 17/03/2022

    /**
     * Devuelve el hash en SHA-256 de la contraseña pasada como parámetro
     * @param contrasenna La contraseña a obtener el hash
     * @return el hash de la contraseña
     */
    public static String cifrarContrasenna(String contrasenna) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(contrasenna.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        }catch(NoSuchAlgorithmException ex) {
            return null;
        }
    }

    /**
     * Método que convierte un vector de bytes en un String
     *      con los valores en hexadecimal equivalentes
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
