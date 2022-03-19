package es.unizar.unoforall.utils;

/**
 * Clase para gestionar caracteres inválidos
 * @author unoforall
 *
 */
public class CaracteresInvalidos {
	//Caracteres inválidos
	private static final String invalidChars = "\'\"\\;";
    
	/**
	 * Método para comprobar si una string contiene caracteres no válidos
	 * @param str	cadena a comprobar
	 * @return 		<b>true</b> si la cadena sólo contiene caracteres válidos y 
	 * 				<b>false</b> en caso contrario
	 */
    public static boolean comprobarCaracteresString(String str) {
    	for(int i=0;i<str.length();i++) {
			if(invalidChars.contains(str.charAt(i) + "")) {
				return false;
			}
		}
    	return true;
    }
}