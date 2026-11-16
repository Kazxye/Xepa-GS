package br.com.xepasolidaria.web;

import br.com.xepasolidaria.application.auth.AutenticacaoService;
import br.com.xepasolidaria.application.auth.CadastroCozinhaCommand;
import br.com.xepasolidaria.application.auth.CadastroFeiranteCommand;
import br.com.xepasolidaria.application.auth.LoginCommand;
import br.com.xepasolidaria.application.auth.PerfilView;
import br.com.xepasolidaria.application.auth.SessaoView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacaoService autenticacaoService;

    public AuthController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/cadastro/feirante")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoView cadastrarFeirante(@Valid @RequestBody CadastroFeiranteCommand comando) {
        return autenticacaoService.cadastrarFeirante(comando);
    }

    @PostMapping("/cadastro/cozinha")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoView cadastrarCozinha(@Valid @RequestBody CadastroCozinhaCommand comando) {
        return autenticacaoService.cadastrarCozinha(comando);
    }

    @PostMapping("/login")
    public SessaoView entrar(@Valid @RequestBody LoginCommand comando) {
        return autenticacaoService.entrar(comando);
    }

    @GetMapping("/me")
    public PerfilView perfil(@AuthenticationPrincipal Jwt jwt) {
        return autenticacaoService.perfil(UsuarioAutenticado.id(jwt));
    }
}
