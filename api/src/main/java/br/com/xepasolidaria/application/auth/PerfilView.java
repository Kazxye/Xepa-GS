package br.com.xepasolidaria.application.auth;

import br.com.xepasolidaria.application.cozinha.CozinhaView;
import br.com.xepasolidaria.application.feirante.FeiranteView;
import br.com.xepasolidaria.domain.usuario.Perfil;

public record PerfilView(
        Long usuarioId,
        String email,
        Perfil perfil,
        String nome,
        FeiranteView feirante,
        CozinhaView cozinha
) {
}
