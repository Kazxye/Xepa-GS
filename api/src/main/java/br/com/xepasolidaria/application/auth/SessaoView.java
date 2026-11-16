package br.com.xepasolidaria.application.auth;

import java.time.Instant;

public record SessaoView(String token, Instant expiraEm, PerfilView perfil) {
}
