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
	 * @return 		<b>false</b> si la cadena sólo contiene caracteres válidos y 
	 * 				<b>true</b> en caso contrario
	 */
    public static boolean hayCaracteresInvalidos(String str) {
    	for(int i=0;i<str.length();i++) {
			if(invalidChars.contains(str.charAt(i) + "")) {
				return true;
			}
		}
    	return false;
    }
}
