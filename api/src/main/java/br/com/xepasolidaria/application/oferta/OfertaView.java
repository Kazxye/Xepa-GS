package br.com.xepasolidaria.application.oferta;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.oferta.Oferta;
import br.com.xepasolidaria.domain.oferta.Reserva;
import br.com.xepasolidaria.domain.oferta.Retirada;
import br.com.xepasolidaria.domain.oferta.StatusOferta;
import br.com.xepasolidaria.domain.oferta.TipoAlimento;

import java.math.BigDecimal;
import java.time.Instant;

public record OfertaView(
        Long id,
        TipoAlimento tipo,
        String descricao,
        BigDecimal quantidadeKg,
        Instant horarioLimite,
        String fotoUrl,
        StatusOferta status,
        Instant criadaEm,
        Double distanciaKm,
        FeiranteResumo feirante,
        FeiraResumo feira,
        ReservaResumo reserva
) {

    public static OfertaView de(Oferta oferta) {
        return de(oferta, null);
    }

    public static OfertaView de(Oferta oferta, Double distanciaKm) {
        return new OfertaView(
                oferta.getId(),
                oferta.getTipo(),
                oferta.getDescricao(),
                oferta.getQuantidadeKg(),
                oferta.getHorarioLimite(),
                oferta.getFotoUrl(),
                oferta.getStatus(),
                oferta.getCriadaEm(),
                distanciaKm,
                FeiranteResumo.de(oferta.getFeirante()),
                FeiraResumo.de(oferta.getFeira()),
                oferta.getReserva().map(ReservaResumo::de).orElse(null)
        );
    }

    public record FeiranteResumo(Long id, String nome, String nomeBarraca, String telefone) {

        static FeiranteResumo de(Feirante feirante) {
            return new FeiranteResumo(feirante.getId(), feirante.getNome(), feirante.getNomeBarraca(),
                    feirante.getTelefone());
        }
    }

    public record FeiraResumo(Long id, String nome, String endereco) {

        static FeiraResumo de(Feira feira) {
            return new FeiraResumo(feira.getId(), feira.getNome(), feira.getEndereco());
        }
    }

    public record ReservaResumo(
            Long cozinhaId,
            String cozinhaNome,
            String responsavel,
            String telefone,
            Instant reservadaEm,
            RetiradaResumo retirada
    ) {

        static ReservaResumo de(Reserva reserva) {
            Cozinha cozinha = reserva.getCozinha();
            return new ReservaResumo(
                    cozinha.getId(),
                    cozinha.getNome(),
                    cozinha.getResponsavel(),
                    cozinha.getTelefone(),
                    reserva.getReservadaEm(),
                    reserva.getRetirada().map(RetiradaResumo::de).orElse(null)
            );
        }
    }

    public record RetiradaResumo(
            BigDecimal pesoKg,
            Instant confirmadoFeiranteEm,
            Instant confirmadoCozinhaEm,
            Instant concluidaEm
    ) {

        static RetiradaResumo de(Retirada retirada) {
            return new RetiradaResumo(
                    retirada.getPesoKg(),
                    retirada.getConfirmadoFeiranteEm(),
                    retirada.getConfirmadoCozinhaEm(),
                    retirada.getConcluidaEm()
            );
        }
    }
}
