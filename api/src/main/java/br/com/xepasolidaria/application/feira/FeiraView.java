package br.com.xepasolidaria.application.feira;

import br.com.xepasolidaria.domain.feira.Feira;

import java.time.DayOfWeek;

public record FeiraView(
        Long id,
        String nome,
        String endereco,
        double latitude,
        double longitude,
        DayOfWeek diaSemana
) {

    public static FeiraView de(Feira feira) {
        return new FeiraView(
                feira.getId(),
                feira.getNome(),
                feira.getEndereco(),
                feira.getLocalizacao().getLatitude(),
                feira.getLocalizacao().getLongitude(),
                feira.getDiaSemana()
        );
    }
}
