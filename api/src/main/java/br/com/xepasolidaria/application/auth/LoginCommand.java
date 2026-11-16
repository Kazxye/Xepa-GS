package br.com.xepasolidaria.application.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String email,
        @NotBlank String senha
) {
}
