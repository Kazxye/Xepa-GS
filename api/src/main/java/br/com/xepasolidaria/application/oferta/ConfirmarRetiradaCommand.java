package br.com.xepasolidaria.application.oferta;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConfirmarRetiradaCommand(
        @NotNull @DecimalMin("0.1") @DecimalMax("1000") BigDecimal pesoKg
) {
}
