package br.com.xepasolidaria.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "xepa")
public record XepaProperties(@NotNull Jwt jwt, @NotNull Cors cors) {

    public record Jwt(@NotBlank @Size(min = 32) String segredo, @NotNull Duration validade) {
    }

    public record Cors(@NotEmpty List<String> origens) {
    }
}
