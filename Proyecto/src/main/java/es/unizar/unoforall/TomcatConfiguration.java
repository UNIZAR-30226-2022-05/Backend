package es.unizar.unoforall;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
public class TomcatConfiguration {
    
	// Este bean nos permite configurar Tomcat, dependiendo de las necesidades puede variar
    // bastante la implementación, por lo cual recomiendo encarecidamente revisar la documentación
    // de Spring Boot e ir incrementalmente añadiendo configuración, según sea necesario.
	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		return new TomcatServletWebServerFactory() {
			
			@Override
			protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {
                // Nos permite cambiar el contexto una vez levantado automáticamente por Spring
                // Boot.

                // Creamos la variable que empleará uno de los beans del proyecto.
				ContextEnvironment environment = new ContextEnvironment();
				environment.setName("customDir");
				environment.setType("java.lang.String");
				environment.setValue("/Users/username/...");
                                
                // Configuramos el data source.
				ContextResource resource = new ContextResource();
				resource.setName("jdbc/UFADB");
				resource.setAuth("Container");
				resource.setType("javax.sql.DataSource");
				resource.setProperty("maxTotal", "100");
				resource.setProperty("maxIdle", "30");
				resource.setProperty("maxWaitMillis", "1000");
				resource.setProperty("driverClassName", "org.postgresql.Driver");
				resource.setProperty("username", "postgresql");
				resource.setProperty("password", "unoforall");
				resource.setProperty("url", "jdbc:postgresql://localhost:5432//uno_for_all_db?currentSchema=uno_for_all");
				resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");

				ContextResourceLink resourceLink = new ContextResourceLink();
				resourceLink.setName("jdbc/unoDB");
				resourceLink.setGlobal("jdbc/UFADB"); //Probar también con  setGlobal("jdbc/SIDB");
				resourceLink.setProperty("author", "Container");
				resourceLink.setType("javax.sql.DataSource");
				
                // Una vez creada la variable de entorno y el recurso del data source los añadimos
                // al contexto.
				context.getNamingResources().addEnvironment(environment);
				context.getNamingResources().addResource(resource);

			}
		};
	}
}
