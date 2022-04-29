package es.unizar.unoforall;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.unizar.unoforall.db.GestorPoolConexionesBD;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		GestorPoolConexionesBD.inicializarPool();
		SpringApplication.run(BackendApplication.class, args);
	}

}
