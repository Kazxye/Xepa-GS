package br.com.xepasolidaria.web;

import br.com.xepasolidaria.application.oferta.ConfirmarRetiradaCommand;
import br.com.xepasolidaria.application.oferta.OfertaService;
import br.com.xepasolidaria.application.oferta.OfertaView;
import br.com.xepasolidaria.application.oferta.PublicarOfertaCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FEIRANTE')")
    public OfertaView publicar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PublicarOfertaCommand comando) {
        return ofertaService.publicar(UsuarioAutenticado.id(jwt), comando);
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('FEIRANTE')")
    public List<OfertaView> minhas(@AuthenticationPrincipal Jwt jwt) {
        return ofertaService.listarDoFeirante(UsuarioAutenticado.id(jwt));
    }

    @PostMapping("/{id}/entrega")
    @PreAuthorize("hasRole('FEIRANTE')")
    public OfertaView confirmarEntrega(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ofertaService.confirmarEntregaPeloFeirante(UsuarioAutenticado.id(jwt), id);
    }

    @GetMapping("/proximas")
    @PreAuthorize("hasRole('COZINHA')")
    public List<OfertaView> proximas(@AuthenticationPrincipal Jwt jwt) {
        return ofertaService.listarProximas(UsuarioAutenticado.id(jwt));
    }

    @GetMapping("/reservadas")
    @PreAuthorize("hasRole('COZINHA')")
    public List<OfertaView> reservadas(@AuthenticationPrincipal Jwt jwt) {
        return ofertaService.listarReservasDaCozinha(UsuarioAutenticado.id(jwt));
    }

    @PostMapping("/{id}/reserva")
    @PreAuthorize("hasRole('COZINHA')")
    public OfertaView reservar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ofertaService.reservar(UsuarioAutenticado.id(jwt), id);
    }

    @PostMapping("/{id}/retirada")
    @PreAuthorize("hasRole('COZINHA')")
    public OfertaView confirmarRetirada(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @Valid @RequestBody ConfirmarRetiradaCommand comando) {
        return ofertaService.confirmarRetiradaPelaCozinha(UsuarioAutenticado.id(jwt), id, comando);
    }
}
