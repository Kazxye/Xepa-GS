package br.com.xepasolidaria.domain.geo;

import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class LocalizacaoTest {

    @Test
    void calculaDistanciaEntreDoisPontosDeSaoPaulo() {
        Localizacao pracaDaSe = new Localizacao(-23.5503, -46.6339);
        Localizacao avenidaPaulista = new Localizacao(-23.5614, -46.6559);

        assertThat(pracaDaSe.distanciaKm(avenidaPaulista)).isCloseTo(2.57, within(0.05));
    }

    @Test
    void distanciaParaOMesmoPontoEZero() {
        Localizacao ponto = new Localizacao(-23.5876, -46.6349);

        assertThat(ponto.distanciaKm(new Localizacao(-23.5876, -46.6349))).isZero();
    }

    @Test
    void rejeitaCoordenadasForaDoIntervalo() {
        assertThatThrownBy(() -> new Localizacao(-91, 0)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Localizacao(0, 181)).isInstanceOf(RegraDeNegocioException.class);
    }
}
