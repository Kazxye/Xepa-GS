package br.com.xepasolidaria.web;

import org.springframework.security.oauth2.jwt.Jwt;

final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
