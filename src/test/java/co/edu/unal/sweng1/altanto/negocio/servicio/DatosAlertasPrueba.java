package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import java.math.BigDecimal;
import java.util.List;


// Datos de prueba de la historia, construidos en un solo lugar.
public final class DatosAlertasPrueba {

    // constructor privado: clase de utilidades, no se crean objetos de ella
    private DatosAlertasPrueba() {
    }

    // texto -> BigDecimal (null se queda null, para probar datos faltantes)
    private static BigDecimal numero(String valor) {
        return (valor == null) ? null : new BigDecimal(valor);
    }

    // Insumo genérico: el nombre es "Insumo " + código. Los valores se pasan como texto.
    public static DatosInsumo insumo(String codigo, String existencia, String umbral, String consumoDiario) {
        return new DatosInsumo(codigo, "Insumo " + codigo, "KILOGRAMO",
                numero(existencia), numero(umbral), numero(consumoDiario));
    }

    // Los cinco insumos de data.sql, con sus valores reales
    public static DatosInsumo harina() {
        return new DatosInsumo("INS-HAR-01", "Harina de Trigo Especial", "KILOGRAMO",
                numero("15.000"), numero("25.000"), numero("35.000"));
    }

    public static DatosInsumo levadura() {
        return new DatosInsumo("INS-LEV-01", "Levadura Fresca en Bloque", "GRAMO",
                numero("1500.000"), numero("500.000"), numero("750.000"));
    }

    public static DatosInsumo margarina() {
        return new DatosInsumo("INS-MAR-01", "Margarina Industrial Hojaldre", "KILOGRAMO",
                numero("12.000"), numero("10.000"), numero("8.000"));
    }

    public static DatosInsumo azucar() {
        return new DatosInsumo("INS-AZU-01", "Azúcar Blanca Refinada", "KILOGRAMO",
                numero("8.500"), numero("10.000"), numero("7.000"));
    }

    public static DatosInsumo queso() {
        return new DatosInsumo("INS-QUE-01", "Queso Campesino Rallado", "KILOGRAMO",
                numero("6.000"), numero("5.000"), numero("6.000"));
    }

    // los cinco juntos, como los devolvería la base de datos
    public static List<DatosInsumo> datosSemilla() {
        return List.of(harina(), levadura(), margarina(), azucar(), queso());
    }

    // Fuente en memoria: implementa el contrato sin base de datos ni mocking.
    // La lista se puede cambiar entre pruebas.
    public static class FuenteEnMemoria implements FuenteInsumos {

        private List<DatosInsumo> datos; // insumos que entrega esta fuente

        public FuenteEnMemoria(List<DatosInsumo> datos) {
            this.datos = datos;
        }

        public void establecer(List<DatosInsumo> nuevosDatos) {
            this.datos = nuevosDatos;
        }

        @Override
        public List<DatosInsumo> obtenerInsumos() {
            return datos;
        }
    }
}