package co.edu.unal.sweng1.altanto.presentacion;

import co.edu.unal.sweng1.altanto.negocio.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.negocio.ConciliacionDuplicadaException;
import co.edu.unal.sweng1.altanto.negocio.ConciliacionNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.ConciliacionService;
import co.edu.unal.sweng1.altanto.negocio.ConteoArticulo;
import co.edu.unal.sweng1.altanto.negocio.DetalleConciliacion;
import co.edu.unal.sweng1.altanto.negocio.InsumoEnAlerta;
import co.edu.unal.sweng1.altanto.negocio.ReglaDeNegocioException;
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
 * <table>
 *   <caption>Rutas</caption>
 *   <tr><th>Método y URL</th><th>Qué hace</th><th>Historia</th></tr>
 *   <tr><td>GET /conciliacion</td><td>Muestra el formulario</td><td>HU-01</td></tr>
 *   <tr><td>POST /conciliacion</td><td>Guarda el borrador</td><td>HU-01</td></tr>
 *   <tr><td>GET /conciliacion/historial</td><td>Lista los cierres previos</td><td>HU-02</td></tr>
 *   <tr><td>GET /conciliacion/{id}</td><td>Muestra las diferencias</td><td>HU-02</td></tr>
 *   <tr><td>POST /conciliacion/{id}/confirmar</td><td>Confirma</td><td>HU-02</td></tr>
 *   <tr><td>POST /conciliacion/{id}/descartar</td><td>Descarta el borrador</td><td>HU-02</td></tr>
 * </table>
 *
 * <p><b>{@code @Controller}</b>: clase que atiende peticiones del navegador. Cada
 * método devuelve el NOMBRE de una plantilla Thymeleaf (por ejemplo,
 * {@code "conciliacion/nueva"} → {@code templates/conciliacion/nueva.html}) o un
 * {@code "redirect:/..."} para mandar al navegador a otra URL.</p>
 *
 * <p>El controlador solo traduce entre la web y el negocio: no calcula nada ni toca la
 * base de datos; eso lo hace {@link ConciliacionService}.</p>
 */
@Controller
@RequestMapping("/conciliacion") // prefijo común de todas las URLs de esta clase
public class ConciliacionController {

    private final ConciliacionService conciliacionService;

    /** Spring inyecta el servicio (ver la explicación en ConciliacionService). */
    public ConciliacionController(ConciliacionService conciliacionService) {
        this.conciliacionService = conciliacionService;
    }

    // =========================================================================
    // CU-01-HU-01: formulario
    // =========================================================================

    /**
     * GET /conciliacion: formulario vacío con la fecha de hoy y un renglón por artículo.
     *
     * <p>{@code Model} es la "bolsa" de datos que el controlador le pasa a la plantilla.
     * Lo que se mete con {@code addAttribute("x", valor)} se lee en el HTML como
     * {@code ${x}}.</p>
     */
    @GetMapping
    public String nueva(Model model) {
        ConciliacionForm form = new ConciliacionForm();
        form.setFecha(LocalDate.now());
        // Un renglón por cada artículo conciliable ("var" deja que Java deduzca el tipo: Articulo).
        for (var articulo : conciliacionService.articulosAConciliar()) {
            form.getLineas().add(new ConciliacionForm.LineaConteo(
                    articulo.getId(), articulo.getNombre(), articulo.getUnidadMedida(), articulo.getTipoArticulo()));
        }
        model.addAttribute("form", form);
        // Extensión 1a: si hoy ya tiene conciliación, se avisa desde el principio.
        conciliacionService.buscarPorFecha(form.getFecha())
                .ifPresent(existente -> model.addAttribute("existenteId", existente.getId()));
        return "conciliacion/nueva";
    }

    /**
     * POST /conciliacion: recibe el formulario y guarda el borrador.
     *
     * <ul>
     *   <li>{@code @Valid @ModelAttribute("form")}: Spring arma el
     *       {@link ConciliacionForm} con lo que envió el navegador y revisa sus
     *       anotaciones de validación.</li>
     *   <li>{@code BindingResult}: guarda los errores de esa validación. DEBE ir justo
     *       después del parámetro validado.</li>
     * </ul>
     *
     * <p>Si todo sale bien, se redirige a la vista de diferencias (patrón
     * Post/Redirect/Get: así, si el usuario recarga la página, no se reenvía el
     * formulario).</p>
     */
    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") ConciliacionForm form,
                            BindingResult errores,
                            Model model) {
        if (errores.hasErrors()) {
            return "conciliacion/nueva"; // se vuelve a pintar el formulario con los errores
        }
        // Se pasa cada fila del formulario al objeto que entiende el servicio.
        List<ConteoArticulo> conteos = form.getLineas().stream()
                .map(l -> new ConteoArticulo(l.getArticuloId(), l.getVendido(), l.getContado()))
                .toList();
        try {
            ConciliacionDiaria guardada =
                    conciliacionService.registrarBorrador(form.getFecha(), form.getTotalVentasZ(), conteos);
            return "redirect:/conciliacion/" + guardada.getId();
        } catch (ConciliacionDuplicadaException e) {
            // Extensión 1a: no se crea otra y se ofrece ver la existente.
            model.addAttribute("error", e.getMessage());
            model.addAttribute("existenteId", e.getIdExistente());
            return "conciliacion/nueva";
        } catch (ReglaDeNegocioException e) {
            // Cualquier otra regla: la transacción ya se deshizo y no se guardó nada.
            model.addAttribute("error", e.getMessage());
            return "conciliacion/nueva";
        }
    }

    // =========================================================================
    // CU-01-HU-02: diferencias y confirmación
    // =========================================================================

    /**
     * GET /conciliacion/historial: lista los cierres previos (borradores y confirmados).
     *
     * <p>Spring elige la ruta más específica: {@code /historial} es un texto fijo, así que
     * gana sobre {@code /{id}}, aunque las dos empiecen por {@code /conciliacion/}.</p>
     */
    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("cierres", conciliacionService.historial());
        return "conciliacion/historial";
    }

    /**
     * GET /conciliacion/{id}: muestra, por artículo, lo esperado, lo contado y la
     * diferencia, y señala los que no cuadran.
     *
     * <p>{@code @PathVariable}: toma el {@code {id}} de la URL y lo pasa como parámetro.</p>
     */
    @GetMapping("/{id}")
    public String resumen(@PathVariable Long id, Model model) {
        ConciliacionDiaria conciliacion;
        try {
            conciliacion = conciliacionService.buscar(id);
        } catch (ConciliacionNoEncontradaException e) {
            // Responde "404 No encontrado" en lugar de un error genérico.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        List<DetalleConciliacion> detalles = conciliacionService.detalles(id);
        model.addAttribute("conciliacion", conciliacion);
        model.addAttribute("detalles", detalles);
        model.addAttribute("conDiferencia", detalles.stream().filter(DetalleConciliacion::tieneDiferencia).count());
        return "conciliacion/resumen";
    }

    /**
     * POST /conciliacion/{id}/confirmar: confirma y muestra la alerta de reabastecimiento.
     *
     * <p>{@code RedirectAttributes.addFlashAttribute}: guarda un dato que sobrevive a UNA
     * redirección y luego desaparece. Sirve para mostrar el mensaje y las alertas
     * después del redirect sin ponerlos en la URL.</p>
     */
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            List<InsumoEnAlerta> alertas = conciliacionService.confirmar(id);
            redirect.addFlashAttribute("mensaje", "Conciliación confirmada. Lo contado quedó como existencia de partida del día siguiente.");
            redirect.addFlashAttribute("alertas", alertas);
        } catch (ReglaDeNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/conciliacion/" + id;
    }

    /** POST /conciliacion/{id}/descartar: borra el borrador; no queda nada guardado. */
    @PostMapping("/{id}/descartar")
    public String descartar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            conciliacionService.descartar(id);
            redirect.addFlashAttribute("mensaje", "Borrador descartado. No se guardó nada.");
            return "redirect:/conciliacion";
        } catch (ReglaDeNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/conciliacion/" + id;
        }
    }
}
