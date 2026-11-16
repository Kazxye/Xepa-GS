package br.com.xepasolidaria.application.feirante;

import br.com.xepasolidaria.application.feira.FeiraView;
import br.com.xepasolidaria.domain.feirante.Feirante;

public record FeiranteView(
        Long id,
        String nome,
        String telefone,
        String nomeBarraca,
        FeiraView feira
) {

    public static FeiranteView de(Feirante feirante) {
        return new FeiranteView(
                feirante.getId(),
                feirante.getNome(),
                feirante.getTelefone(),
                feirante.getNomeBarraca(),
                FeiraView.de(feirante.getFeira())
        );
    }
}
