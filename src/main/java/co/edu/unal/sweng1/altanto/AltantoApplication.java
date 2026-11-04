package co.edu.unal.sweng1.altanto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la aplicación (como el {@code if __name__ == "__main__"} de Python).
 *
 * <p>{@code @SpringBootApplication} hace tres cosas:</p>
 * <ul>
 *   <li>Escanea ESTE paquete y todos sus subpaquetes ({@code presentacion}, {@code negocio},
 *       {@code persistencia}) buscando clases con {@code @Controller}, {@code @Service},
 *       {@code @Repository}, {@code @Entity}... y crea sus objetos. Por eso esta clase debe
 *       estar en el paquete raíz.</li>
 *   <li>Autoconfigura lo que encuentra en el {@code pom.xml}: servidor web (Tomcat), base
 *       de datos H2, Thymeleaf, JPA.</li>
 *   <li>Permite agregar configuración propia aquí (hoy no hace falta).</li>
 * </ul>
 */
@SpringBootApplication
public class AltantoApplication {

	/** Arranca Spring y el servidor web en el puerto 8080. */
	public static void main(String[] args) {
		SpringApplication.run(AltantoApplication.class, args);
	}

}
