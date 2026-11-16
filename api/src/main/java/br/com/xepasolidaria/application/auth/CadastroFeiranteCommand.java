package br.com.xepasolidaria.application.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroFeiranteCommand(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String senha,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Size(max = 120) String nomeBarraca,
        @NotNull Long feiraId
) {
}
