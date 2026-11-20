package co.edu.unal.sweng1.altanto.presentacion.controlador;

import co.edu.unal.sweng1.altanto.negocio.dto.CantidadRecibidaPedido;
import co.edu.unal.sweng1.altanto.negocio.excepcion.PedidoNoEncontradoException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.negocio.servicio.RecepcionPedidoService;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetallePedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Pedido;
import co.edu.unal.sweng1.altanto.presentacion.formulario.RecepcionPedidoForm;
import jakarta.validation.Valid;
import java.util.ArrayList;
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

/** Pantallas del flujo de recepción de pedidos (CU-04-HU-01). */
@Controller
@RequestMapping("/recepcion-pedidos")
public class RecepcionPedidoController {

    private static final String VISTA_RECEPCION = "recepcion/recibir";

    private final RecepcionPedidoService recepcionPedidoService;

    public RecepcionPedidoController(RecepcionPedidoService recepcionPedidoService) {
        this.recepcionPedidoService = recepcionPedidoService;
    }

    @GetMapping("/pendientes")
    public String pendientes(Model model) {
        model.addAttribute("pedidos", recepcionPedidoService.pedidosPendientes());
        return "recepcion/pendientes";
    }

    @GetMapping("/{pedidoId}")
    public String formulario(@PathVariable Long pedidoId, Model model,
                             RedirectAttributes redirect) {
        try {
            cargarFormulario(pedidoId, new RecepcionPedidoForm(), model);
            return VISTA_RECEPCION;
        } catch (PedidoNoEncontradoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ReglaDeNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/recepcion-pedidos/pendientes";
        }
    }

    @PostMapping("/{pedidoId}")
    public String recibir(@PathVariable Long pedidoId,
                          @Valid @ModelAttribute("form") RecepcionPedidoForm form,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            try {
                cargarFormulario(pedidoId, form, model);
                return VISTA_RECEPCION;
            } catch (PedidoNoEncontradoException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (ReglaDeNegocioException e) {
                redirect.addFlashAttribute("error", e.getMessage());
                return "redirect:/recepcion-pedidos/pendientes";
            }
        }

        List<CantidadRecibidaPedido> cantidades = form.getLineas().stream()
                .map(linea -> new CantidadRecibidaPedido(
                        linea.getDetallePedidoId(), linea.getCantidadRecibida()))
                .toList();
        try {
            recepcionPedidoService.recibir(pedidoId, cantidades);
            redirect.addFlashAttribute("mensaje", "Pedido recibido y cerrado.");
        } catch (PedidoNoEncontradoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ReglaDeNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recepcion-pedidos/pendientes";
    }

    private void cargarFormulario(Long pedidoId, RecepcionPedidoForm form, Model model) {
        Pedido pedido = recepcionPedidoService.buscarPendiente(pedidoId);
        List<DetallePedido> detalles = recepcionPedidoService.detalles(pedidoId);
        List<RecepcionPedidoForm.LineaRecepcion> enviadas = form.getLineas();
        List<RecepcionPedidoForm.LineaRecepcion> lineas = new ArrayList<>();
        for (int i = 0; i < detalles.size(); i++) {
            RecepcionPedidoForm.LineaRecepcion linea =
                    new RecepcionPedidoForm.LineaRecepcion(detalles.get(i).getId());
            if (enviadas != null && i < enviadas.size() && enviadas.get(i) != null) {
                linea.setCantidadRecibida(enviadas.get(i).getCantidadRecibida());
            }
            lineas.add(linea);
        }
        form.setLineas(lineas);
        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);
        model.addAttribute("form", form);
    }
}
