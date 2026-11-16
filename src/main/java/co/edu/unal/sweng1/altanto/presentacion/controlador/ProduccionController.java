package co.edu.unal.sweng1.altanto.presentacion.controlador;

import co.edu.unal.sweng1.altanto.negocio.dto.InsumoGastado;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.negocio.servicio.TandaProduccionService;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import co.edu.unal.sweng1.altanto.presentacion.formulario.TandaForm;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas del CU-03: Registrar una tanda de producción.
 *
 * <pre>
 *   GET  /produccion/tandas        formulario                      (HU-01)
 *   POST /produccion/tandas        registra y descuenta insumos     (HU-01 y HU-02)
 *   GET  /produccion/tandas/{id}   resumen de la tanda registrada   (HU-01)
 * </pre>
 *
 * <p>{@code @Controller}: cada método atiende una URL y devuelve el NOMBRE de una
 * plantilla. El controlador no calcula nada: traduce entre la web y el servicio. Mismo
 * patrón que {@link ConciliacionController} en el CU-01.</p>
 */
@Controller
@RequestMapping("/produccion/tandas") // prefijo de todas las URLs de esta clase
public class ProduccionController {

    private final TandaProduccionService tandaService;

    /** Spring inyecta el servicio por constructor. */
    public ProduccionController(TandaProduccionService tandaService) {
        this.tandaService = tandaService;
    }

    /**
     * GET /produccion/tandas: formulario con la fecha de hoy, el combo de productos
     * horneados y una fila por insumo.
     */
    @GetMapping
    public String nueva(Model model) {
        TandaForm form = new TandaForm();
        form.setFecha(LocalDate.now());
        for (Articulo insumo : tandaService.insumosDisponibles()) {
            form.getLineas().add(new TandaForm.LineaInsumo(
                    insumo.getId(), insumo.getNombre(), insumo.getUnidadMedida()));
        }
        model.addAttribute("form", form);
        model.addAttribute("productos", tandaService.productosElaborados());
        return "produccion/nueva-tanda";
    }

    /**
     * POST /produccion/tandas: registra la tanda y descuenta los insumos.
     * <ul>
     *   <li>{@code @Valid @ModelAttribute}: Spring llena el {@link TandaForm} y revisa sus
     *       validaciones de formato.</li>
     *   <li>Si hay errores de formato → vuelve a pintar el formulario.</li>
     *   <li>Si una regla de negocio falla (un insumo no alcanza) → muestra el mensaje.</li>
     * </ul>
     * Si sale bien, redirige al resumen (así recargar la página no reenvía el formulario).
     */
    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") TandaForm form,
                            BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            model.addAttribute("productos", tandaService.productosElaborados());
            return "produccion/nueva-tanda"; // vuelve a pintar el formulario con los errores
        }
        // Cada fila del formulario se pasa al objeto que entiende el servicio.
        List<InsumoGastado> gastados = form.getLineas().stream()
                .map(l -> new InsumoGastado(l.getInsumoId(), l.getCantidad()))
                .toList();
        try {
            TandaProduccion guardada = tandaService.registrarTanda(
                    form.getFecha(), form.getArticuloProductoId(), form.getHarinaBaseKg(),
                    form.getUnidadesObtenidas(), form.getMaestroPanadero(),
                    form.getObservaciones(), gastados);
            return "redirect:/produccion/tandas/" + guardada.getId();
        } catch (ReglaDeNegocioException e) {
            // Incluye InsumoInsuficienteException: su mensaje nombra el insumo que no alcanzó.
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productos", tandaService.productosElaborados());
            return "produccion/nueva-tanda";
        }
    }

    /** GET /produccion/tandas/{id}: el resumen de una tanda. {@code @PathVariable} toma el {id}. */
    @GetMapping("/{id}")
    public String resumen(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("tanda", tandaService.buscar(id));
        } catch (ReglaDeNegocioException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()); // responde 404
        }
        model.addAttribute("detalles", tandaService.detalles(id));
        return "produccion/resumen";
    }
}
