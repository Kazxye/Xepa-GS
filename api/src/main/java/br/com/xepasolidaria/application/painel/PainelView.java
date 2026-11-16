package br.com.xepasolidaria.application.painel;

import java.math.BigDecimal;
import java.util.List;

public record PainelView(
        BigDecimal quilosRecuperados,
        long retiradasConcluidas,
        long cozinhasAtendidas,
        long feirantesDoadores,
        long ofertasPublicadas,
        double taxaDeRetiradaPercentual,
        Double tempoMedioAteReservaMinutos,
        List<IndicadorFeira> porFeira
) {

    public record IndicadorFeira(
            Long feiraId,
            String feiraNome,
            BigDecimal quilosRecuperados,
            long retiradasConcluidas,
            long ofertasPublicadas
    ) {
    }
}
