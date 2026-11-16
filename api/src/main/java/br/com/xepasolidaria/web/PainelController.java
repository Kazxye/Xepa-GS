package br.com.xepasolidaria.web;

import br.com.xepasolidaria.application.painel.PainelService;
import br.com.xepasolidaria.application.painel.PainelView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping
    public PainelView obter() {
        return painelService.obter();
    }
}
