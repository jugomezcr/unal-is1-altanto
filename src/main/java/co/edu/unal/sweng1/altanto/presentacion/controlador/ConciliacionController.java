package co.edu.unal.sweng1.altanto.presentacion.controlador;

import co.edu.unal.sweng1.altanto.negocio.dto.ConteoArticulo;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionDuplicadaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.negocio.servicio.ConciliacionService;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.presentacion.formulario.ConciliacionForm;
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
 * Pantallas del CU-01: Conciliar el día.
 *
 * <pre>
 *   GET  /conciliacion                 formulario                  (HU-01)
 *   POST /conciliacion                 guarda el borrador          (HU-01)
 *   GET  /conciliacion/historial       cierres previos             (HU-02)
 *   GET  /conciliacion/{id}            diferencias                 (HU-02)
 *   POST /conciliacion/{id}/confirmar  confirma                    (HU-02)
 * </pre>
 *
 * <p>{@code @Controller}: cada método atiende una URL (como {@code @app.get} en Flask) y
 * devuelve el NOMBRE de una plantilla: {@code "conciliacion/nueva"} →
 * {@code templates/conciliacion/nueva.html}. El controlador no calcula nada: solo traduce
 * entre la web y el servicio.</p>
 */
@Controller
@RequestMapping("/conciliacion") // prefijo de todas las URLs de esta clase
public class ConciliacionController {

    private final ConciliacionService conciliacionService;

    /** Spring inyecta el servicio por constructor. */
    public ConciliacionController(ConciliacionService conciliacionService) {
        this.conciliacionService = conciliacionService;
    }

    /**
     * GET /conciliacion: formulario con la fecha de hoy y un renglón por artículo.
     * {@code Model} es la "bolsa" de datos para la plantilla: {@code addAttribute("form", x)}
     * se lee en el HTML como {@code ${form}}.
     */
    @GetMapping
    public String nueva(Model model) {
        ConciliacionForm form = new ConciliacionForm();
        form.setFecha(LocalDate.now());
        for (Articulo articulo : conciliacionService.articulosAConciliar()) {
            form.getLineas().add(new ConciliacionForm.LineaConteo(
                    articulo.getId(), articulo.getNombre(), articulo.getUnidadMedida(), articulo.getTipoArticulo()));
        }
        model.addAttribute("form", form);
        return "conciliacion/nueva";
    }

    /**
     * POST /conciliacion: recibe el formulario y guarda el borrador.
     * <ul>
     *   <li>{@code @Valid @ModelAttribute}: Spring llena el {@link ConciliacionForm} con lo
     *       enviado y revisa sus validaciones.</li>
     *   <li>{@code BindingResult}: los errores de esa validación (va justo después).</li>
     * </ul>
     * Si sale bien, redirige a las diferencias (así recargar la página no reenvía el form).
     */
    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") ConciliacionForm form,
                            BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            return "conciliacion/nueva"; // vuelve a pintar el formulario con los errores
        }
        // Cada fila del formulario se pasa al objeto que entiende el servicio.
        List<ConteoArticulo> conteos = form.getLineas().stream()
                .map(l -> new ConteoArticulo(l.getArticuloId(), l.getVendido(), l.getContado()))
                .toList();
        try {
            ConciliacionDiaria guardada =
                    conciliacionService.registrarBorrador(form.getFecha(), form.getTotalVentasZ(), conteos);
            return "redirect:/conciliacion/" + guardada.getId();
        } catch (ConciliacionDuplicadaException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("existenteId", e.getIdExistente()); // para el enlace "ver la existente"
            return "conciliacion/nueva";
        } catch (ReglaDeNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "conciliacion/nueva";
        }
    }

    /**
     * GET /conciliacion/historial. Spring prefiere la ruta fija {@code /historial} sobre
     * {@code /{id}}, aunque las dos empiecen igual.
     */
    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("cierres", conciliacionService.historial());
        return "conciliacion/historial";
    }

    /** GET /conciliacion/{id}: las diferencias por artículo. {@code @PathVariable} toma el {id} de la URL. */
    @GetMapping("/{id}")
    public String resumen(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("conciliacion", conciliacionService.buscar(id));
        } catch (ConciliacionNoEncontradaException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()); // responde 404
        }
        model.addAttribute("detalles", conciliacionService.detalles(id));
        return "conciliacion/resumen";
    }

    /**
     * POST /conciliacion/{id}/confirmar. {@code addFlashAttribute} guarda un mensaje que
     * sobrevive a UNA redirección (como {@code flash()} de Flask).
     */
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            conciliacionService.confirmar(id);
            redirect.addFlashAttribute("mensaje", "Conciliación confirmada. Lo contado quedó como existencia actual.");
        } catch (ReglaDeNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/conciliacion/" + id;
    }
}
