package br.com.xepasolidaria.web;

import br.com.xepasolidaria.application.feira.FeiraService;
import br.com.xepasolidaria.application.feira.FeiraView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feiras")
public class FeiraController {

    private final FeiraService feiraService;

    public FeiraController(FeiraService feiraService) {
        this.feiraService = feiraService;
    }

    @GetMapping
    public List<FeiraView> listar() {
        return feiraService.listar();
    }
}
