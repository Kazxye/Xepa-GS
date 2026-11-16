package br.com.xepasolidaria.application.oferta;

import br.com.xepasolidaria.domain.oferta.TipoAlimento;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record PublicarOfertaCommand(
        @NotNull TipoAlimento tipo,
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @DecimalMin("0.1") @DecimalMax("1000") BigDecimal quantidadeKg,
        @NotNull Instant horarioLimite,
        @Size(max = 500) @Pattern(regexp = "^https?://\\S+$", message = "deve ser um endereço http ou https") String fotoUrl
) {
}
