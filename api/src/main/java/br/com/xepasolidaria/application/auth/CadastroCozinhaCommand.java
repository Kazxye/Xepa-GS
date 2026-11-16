package br.com.xepasolidaria.application.auth;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CadastroCozinhaCommand(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 120) String responsavel,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String senha,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Size(max = 200) String endereco,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @NotNull @DecimalMin("0.1") @DecimalMax("20") Double raioBuscaKm,
        @NotNull @DecimalMin("1") @DecimalMax("1000") BigDecimal capacidadeCargaKg
) {
}
