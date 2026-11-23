package co.edu.unal.sweng1.altanto.presentacion.controlador;

import co.edu.unal.sweng1.altanto.negocio.dto.Sobrecosto;
import co.edu.unal.sweng1.altanto.negocio.excepcion.CompraEmergenciaNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.negocio.servicio.CompraEmergenciaService;
import co.edu.unal.sweng1.altanto.persistencia.entidad.CompraEmergencia;
import co.edu.unal.sweng1.altanto.presentacion.formulario.CompraEmergenciaForm;
import jakarta.validation.Valid;
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
 * Pantallas de CU-04-HU-02: compra de emergencia.
 *
 * <pre>
 *   GET  /recepcion-pedidos/emergencia          formulario
 *   POST /recepcion-pedidos/emergencia/revisar  muestra la diferencia (no guarda)
 *   POST /recepcion-pedidos/emergencia          confirma: guarda y suma existencias
 *   GET  /recepcion-pedidos/emergencia/{id}     resumen de la compra
 * </pre>
 *
 * <p>Cancelar es un enlace al formulario: no envía nada, por eso no se guarda nada.
 * Nota: la ruta literal {@code /emergencia} gana a {@code /{pedidoId}} de
 * {@code RecepcionPedidoController} (Spring prefiere la más específica).</p>
 */
@Controller
@RequestMapping("/recepcion-pedidos/emergencia")
public class CompraEmergenciaController {

    private static final String VISTA_FORMULARIO = "recepcion/emergencia";

    private final CompraEmergenciaService service;

    public CompraEmergenciaController(CompraEmergenciaService service) {
        this.service = service;
    }

    /** GET: formulario vacío con la lista de artículos. */
    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("form", new CompraEmergenciaForm());
        model.addAttribute("articulos", service.articulosComprables());
        return VISTA_FORMULARIO;
    }

    /**
     * POST /revisar: valida y muestra la diferencia en pesos y en % para que el
     * Administrador confirme. {@code @Valid} aplica las reglas de formato del formulario;
     * si fallan, {@code BindingResult} trae los errores y se vuelve a pintar el formulario.
     */
    @PostMapping("/revisar")
    public String revisar(@Valid @ModelAttribute("form") CompraEmergenciaForm form,
                          BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            model.addAttribute("articulos", service.articulosComprables());
            return VISTA_FORMULARIO;
        }
        try {
            Sobrecosto sobrecosto = service.calcularSobrecosto(
                    form.getCantidadRecibida(), form.getPrecioPagado(), form.getPrecioHabitual());
            model.addAttribute("articulo", service.articuloComprable(form.getArticuloId()));
            model.addAttribute("sobrecosto", sobrecosto);
            return "recepcion/emergencia-revisar";
        } catch (ReglaDeNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("articulos", service.articulosComprables());
            return VISTA_FORMULARIO;
        }
    }

    /** POST: confirma. Si sale bien, redirige al resumen (recargar no reenvía el formulario). */
    @PostMapping
    public String confirmar(@Valid @ModelAttribute("form") CompraEmergenciaForm form,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("articulos", service.articulosComprables());
            return VISTA_FORMULARIO;
        }
        try {
            CompraEmergencia compra = service.registrar(form.getArticuloId(),
                    form.getCantidadRecibida(), form.getPrecioPagado(), form.getPrecioHabitual());
            redirect.addFlashAttribute("mensaje",
                    "Compra de emergencia registrada y existencias actualizadas.");
            return "redirect:/recepcion-pedidos/emergencia/" + compra.getId();
        } catch (ReglaDeNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("articulos", service.articulosComprables());
            return VISTA_FORMULARIO;
        }
    }

    /** GET /{id}: resumen de una compra. Si no existe, responde 404. */
    @GetMapping("/{id}")
    public String resumen(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("compra", service.buscar(id));
        } catch (CompraEmergenciaNoEncontradaException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return "recepcion/emergencia-resumen";
    }
}
