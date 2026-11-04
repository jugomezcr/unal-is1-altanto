package co.edu.unal.sweng1.altanto.presentacion;

import co.edu.unal.sweng1.altanto.negocio.ExistenciaInsuficienteException;
import co.edu.unal.sweng1.altanto.negocio.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.negocio.TandaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Screens of CU-03-HU-01: register a production batch.
 *
 * <table>
 *   <caption>Routes</caption>
 *   <tr><th>Method and URL</th><th>What it does</th></tr>
 *   <tr><td>GET /produccion/tandas</td><td>Empty form with today's date</td></tr>
 *   <tr><td>POST /produccion/tandas</td><td>Saves the batch (Post/Redirect/Get on success)</td></tr>
 * </table>
 *
 * <p>Model contract of the {@code produccion/nueva-tanda} template:</p>
 * <ul>
 *   <li>{@code tandaForm} — the bound {@link TandaForm}.</li>
 *   <li>{@code productos} — active {@code PRODUCTO_ELABORADO} articles.</li>
 *   <li>{@code insumos} — active {@code INSUMO} articles, with their stock.</li>
 *   <li>{@code error} — business error banner (string, only on failure).</li>
 *   <li>{@code success} — flash banner after a successful redirect.</li>
 * </ul>
 *
 * <p>The controller only translates between the web and the business: it never
 * touches the repositories; that is {@link TandaService}'s job.</p>
 */
@Controller
@RequestMapping("/produccion/tandas")
public class TandaController {

    private final TandaService tandaService;

    /** Spring injects the service (constructor injection, as in ConciliacionController). */
    public TandaController(TandaService tandaService) {
        this.tandaService = tandaService;
    }

    // =========================================================================
    // CU-03-HU-01: form
    // =========================================================================

    /** GET /produccion/tandas: empty form with today's date and both catalogs. */
    @GetMapping
    public String nueva(Model model) {
        TandaForm form = new TandaForm();
        form.setFecha(LocalDate.now());
        model.addAttribute("tandaForm", form);
        cargarCatalogos(model);
        return "produccion/nueva-tanda";
    }

    /**
     * POST /produccion/tandas: validates the form and registers the batch.
     *
     * <ul>
     *   <li>{@code @Valid @ModelAttribute("tandaForm")}: Spring binds the request into
     *       the {@link TandaForm} and runs its jakarta validations. {@code BindingResult}
     *       must come right after the validated parameter.</li>
     *   <li><b>Step 4a:</b> an {@link ExistenciaInsuficienteException} comes back to the
     *       SAME form (no redirect) with the "Insumo X insuficiente" banner; the
     *       transaction already rolled back, so no stock was discounted.</li>
     *   <li>Success follows Post/Redirect/Get: the GET shows the success flash banner.</li>
     * </ul>
     */
    @PostMapping
    public String registrar(@Valid @ModelAttribute("tandaForm") TandaForm form,
                            BindingResult errores,
                            Model model,
                            RedirectAttributes redirect) {
        // The catalogs are needed again to re-render the form on any failure.
        cargarCatalogos(model);
        if (errores.hasErrors()) {
            return "produccion/nueva-tanda";
        }
        try {
            tandaService.registrarTanda(form.getFecha(), form.getProductoId(), form.getHarinaBaseKg(),
                    form.getUnidadesObtenidas(), form.getMaestroPanadero(), form.getObservaciones(),
                    form.getInsumoIds(), form.getCantidades());
        } catch (ExistenciaInsuficienteException e) {
            // Step 4a: message already reads "Insumo X insuficiente ...".
            model.addAttribute("error", e.getMessage());
            return "produccion/nueva-tanda";
        } catch (ReglaDeNegocioException e) {
            // Any other rule: the transaction rolled back and nothing was saved.
            model.addAttribute("error", e.getMessage());
            return "produccion/nueva-tanda";
        }
        redirect.addFlashAttribute("success",
                "Tanda registrada. Las existencias de los insumos se descontaron.");
        return "redirect:/produccion/tandas";
    }

    /** Puts the two selects of the form into the model (products and inputs). */
    private void cargarCatalogos(Model model) {
        model.addAttribute("productos", tandaService.productosElaborados());
        model.addAttribute("insumos", tandaService.insumos());
    }
}
