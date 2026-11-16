package br.com.xepasolidaria.application.cozinha;

import br.com.xepasolidaria.domain.cozinha.Cozinha;

import java.math.BigDecimal;

public record CozinhaView(
        Long id,
        String nome,
        String responsavel,
        String telefone,
        String endereco,
        double latitude,
        double longitude,
        double raioBuscaKm,
        BigDecimal capacidadeCargaKg
) {

    public static CozinhaView de(Cozinha cozinha) {
        return new CozinhaView(
                cozinha.getId(),
                cozinha.getNome(),
                cozinha.getResponsavel(),
                cozinha.getTelefone(),
                cozinha.getEndereco(),
                cozinha.getLocalizacao().getLatitude(),
                cozinha.getLocalizacao().getLongitude(),
                cozinha.getRaioBuscaKm(),
                cozinha.getCapacidadeCargaKg()
        );
    }
}
