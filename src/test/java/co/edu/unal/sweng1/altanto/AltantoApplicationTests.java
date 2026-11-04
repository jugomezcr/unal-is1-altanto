package co.edu.unal.sweng1.altanto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba "de humo": la aplicación arranca. Si falla, hay un problema de configuración
 * (rutas duplicadas, una entidad que no coincide con schema.sql...).
 */
@SpringBootTest // arranca la aplicación completa con H2 en memoria
class AltantoApplicationTests {

	/** Vacía a propósito: si Spring no logra arrancar, esta prueba falla. */
	@Test
	void contextLoads() {
	}

}
