package br.com.xepasolidaria.application.auth;

import br.com.xepasolidaria.domain.usuario.Usuario;

public interface TokenService {

    TokenEmitido emitir(Usuario usuario);
}
