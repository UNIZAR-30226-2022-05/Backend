package es.unizar.unoforall.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Clase para gestionar el envío de correos electrónicos
 *
 */
public class Mail {
	
	//Usuario y contraseña para enviar correos electrónicos
    private static final String username;
    private static String password;
    
    //true si no se ha encontrado el archivo de crendenciales
    private static boolean faltanCredenciales; 
	
    static {
    	faltanCredenciales = false;
    	Properties propiedades = new Properties();
    	try {
			propiedades.load(new FileReader("../credenciales.properties"));
		} catch (FileNotFoundException e) {
			System.err.println("No se encuentra el fichero credenciales.properties");
			faltanCredenciales = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	username = propiedades.getProperty("mail");
    	password = propiedades.getProperty("clave_mail");
    }
    
    /**
     * Método para enviar un correo electrónico a un determinado destinatario con un asunto y un cuerpo
     * @param destinatario El destinatario del mensaje. Debe ser una dirección válida
     * @param asunto 	El asunto del mensaje
     * @param cuerpo 	El cuerpo del mensaje
     * @return 			True si el correo se envió correctamente
     */
	public static boolean sendMail(String destinatario, String asunto, String cuerpo) {
		if (faltanCredenciales) {
			return false;
		}
		
		Transport t = null;
		try {
			Properties prop = new Properties();
			prop.put("mail.smtp.host", "smtp.gmail.com");
	        prop.put("mail.smtp.port", "587");
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.starttls.enable", "true"); //TLS
	        
	        Session session = Session.getInstance(prop,
	                new javax.mail.Authenticator() {
	                    protected PasswordAuthentication getPasswordAuthentication() {
	                        return new PasswordAuthentication(username, password);
	                    }
	                });
	        
	        MimeMessage message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(username));
	        message.setRecipients(
	                Message.RecipientType.TO,
	                InternetAddress.parse(destinatario)
	        );
	        message.setSubject(asunto, "UTF-8");
	        message.setText(cuerpo, "UTF-8");

	        t = session.getTransport("smtp");
	        t.connect(username, password);
	        t.sendMessage(message, message.getAllRecipients());
	        t.close();
	        return true;
		}catch(SendFailedException e) {
			return false;
		}catch(Exception ex) {
			ex.printStackTrace();
			if(t != null) {
				try {
					t.close();
				}catch(Exception ex2) {					
				}				
			}
			return false;
		}
	}
}


