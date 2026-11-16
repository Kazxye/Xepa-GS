package br.com.xepasolidaria.domain.oferta;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.geo.Localizacao;
import br.com.xepasolidaria.domain.shared.ConflitoException;
import br.com.xepasolidaria.domain.shared.OperacaoNaoPermitidaException;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import br.com.xepasolidaria.domain.usuario.Perfil;
import br.com.xepasolidaria.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfertaTest {

    private static final Instant AGORA = Instant.parse("2026-11-14T14:00:00Z");

    private Feirante feirante;
    private Cozinha cozinhaProxima;
    private Cozinha outraCozinhaProxima;
    private Cozinha cozinhaDistante;

    @BeforeEach
    void preparar() {
        Feira feira = new Feira("Feira Teste", "Rua Teste", new Localizacao(-23.5876, -46.6349), DayOfWeek.SATURDAY);
        feirante = comId(new Feirante(usuario(Perfil.FEIRANTE), feira, "Joana", "11999990000", "Frutas da Joana"), 1L);
        cozinhaProxima = comId(cozinha(new Localizacao(-23.5890, -46.6365), 2.0), 10L);
        outraCozinhaProxima = comId(cozinha(new Localizacao(-23.5862, -46.6380), 2.0), 11L);
        cozinhaDistante = comId(cozinha(new Localizacao(-23.5503, -46.6339), 2.0), 12L);
    }

    @Test
    void publicaOfertaDisponivel() {
        Oferta oferta = novaOferta();

        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.DISPONIVEL);
        assertThat(oferta.estaDisponivelEm(AGORA)).isTrue();
        assertThat(oferta.getReserva()).isEmpty();
    }

    @Test
    void naoPublicaComHorarioLimiteNoPassado() {
        assertThatThrownBy(() -> Oferta.publicar(feirante, TipoAlimento.FRUTA, "Banana", new BigDecimal("10"),
                AGORA.minusSeconds(60), null, AGORA))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void naoPublicaComHorarioLimiteAlemDe24Horas() {
        assertThatThrownBy(() -> Oferta.publicar(feirante, TipoAlimento.FRUTA, "Banana", new BigDecimal("10"),
                AGORA.plus(Duration.ofHours(25)), null, AGORA))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void naoPublicaComQuantidadeInvalida() {
        assertThatThrownBy(() -> Oferta.publicar(feirante, TipoAlimento.FRUTA, "Banana", BigDecimal.ZERO,
                AGORA.plus(Duration.ofHours(2)), null, AGORA))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void cozinhaDentroDoRaioReservaAOferta() {
        Oferta oferta = novaOferta();

        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.RESERVADA);
        assertThat(oferta.getReserva()).isPresent();
        assertThat(oferta.getReserva().get().pertenceA(cozinhaProxima)).isTrue();
    }

    @Test
    void segundaCozinhaNaoConsegueReservar() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        assertThatThrownBy(() -> oferta.reservar(outraCozinhaProxima, AGORA.plusSeconds(120)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void mesmaCozinhaNaoReservaDuasVezes() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        assertThatThrownBy(() -> oferta.reservar(cozinhaProxima, AGORA.plusSeconds(120)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("Sua cozinha");
    }

    @Test
    void cozinhaForaDoRaioNaoReserva() {
        Oferta oferta = novaOferta();

        assertThatThrownBy(() -> oferta.reservar(cozinhaDistante, AGORA.plusSeconds(60)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("raio");
    }

    @Test
    void naoReservaDepoisDoHorarioLimite() {
        Oferta oferta = novaOferta();

        assertThatThrownBy(() -> oferta.reservar(cozinhaProxima, AGORA.plus(Duration.ofHours(3))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("horário limite");
    }

    @Test
    void retiradaSoEConcluidaQuandoAsDuasPartesConfirmam() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        oferta.confirmarRetiradaPelaCozinha(cozinhaProxima, new BigDecimal("11.5"), AGORA.plusSeconds(600));
        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.RESERVADA);
        assertThat(oferta.pesoRetirado()).isEmpty();

        oferta.confirmarRetiradaPeloFeirante(feirante, AGORA.plusSeconds(660));
        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.RETIRADA);
        assertThat(oferta.pesoRetirado()).contains(new BigDecimal("11.5"));
    }

    @Test
    void outraCozinhaNaoConfirmaRetirada() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        assertThatThrownBy(() -> oferta.confirmarRetiradaPelaCozinha(outraCozinhaProxima, BigDecimal.TEN,
                AGORA.plusSeconds(600)))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    void feiranteNaoConfirmaDuasVezes() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));
        oferta.confirmarRetiradaPeloFeirante(feirante, AGORA.plusSeconds(600));

        assertThatThrownBy(() -> oferta.confirmarRetiradaPeloFeirante(feirante, AGORA.plusSeconds(700)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void naoConfirmaRetiradaSemReserva() {
        Oferta oferta = novaOferta();

        assertThatThrownBy(() -> oferta.confirmarRetiradaPeloFeirante(feirante, AGORA.plusSeconds(600)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void ofertaDisponivelExpiraNoHorarioLimite() {
        Oferta oferta = novaOferta();

        assertThat(oferta.expirarSeVencida(AGORA.plus(Duration.ofMinutes(119)))).isFalse();
        assertThat(oferta.expirarSeVencida(AGORA.plus(Duration.ofHours(2)))).isTrue();
        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.EXPIRADA);
    }

    @Test
    void ofertaReservadaTemToleranciaAntesDeExpirar() {
        Oferta oferta = novaOferta();
        oferta.reservar(cozinhaProxima, AGORA.plusSeconds(60));

        assertThat(oferta.expirarSeVencida(AGORA.plus(Duration.ofMinutes(150)))).isFalse();
        assertThat(oferta.expirarSeVencida(AGORA.plus(Duration.ofHours(3)))).isTrue();
        assertThat(oferta.getStatus()).isEqualTo(StatusOferta.EXPIRADA);
    }

    private Oferta novaOferta() {
        return Oferta.publicar(feirante, TipoAlimento.FRUTA, "Banana nanica", new BigDecimal("12"),
                AGORA.plus(Duration.ofHours(2)), null, AGORA);
    }

    private static Usuario usuario(Perfil perfil) {
        return new Usuario("teste-" + perfil.name().toLowerCase() + "@xepa.app", "hash", perfil, AGORA);
    }

    private static Cozinha cozinha(Localizacao localizacao, double raioKm) {
        return new Cozinha(usuario(Perfil.COZINHA), "Cozinha", "Responsável", "11988887777", "Rua A",
                localizacao, raioKm, new BigDecimal("50"));
    }

    private static <T> T comId(T entidade, Long id) {
        ReflectionTestUtils.setField(entidade, "id", id);
        return entidade;
    }
}
