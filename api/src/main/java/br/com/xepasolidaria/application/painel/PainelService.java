package br.com.xepasolidaria.application.painel;

import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.oferta.Oferta;
import br.com.xepasolidaria.domain.oferta.OfertaRepository;
import br.com.xepasolidaria.domain.oferta.Reserva;
import br.com.xepasolidaria.domain.oferta.StatusOferta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class PainelService {

    private final OfertaRepository ofertaRepository;

    public PainelService(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    @Transactional(readOnly = true)
    public PainelView obter() {
        List<Oferta> ofertas = ofertaRepository.findAll();
        List<Oferta> retiradas = ofertas.stream()
                .filter(oferta -> oferta.getStatus() == StatusOferta.RETIRADA)
                .toList();

        long cozinhasAtendidas = retiradas.stream()
                .map(oferta -> oferta.getReserva().orElseThrow().getCozinha().getId())
                .distinct()
                .count();
        long feirantesDoadores = retiradas.stream()
                .map(oferta -> oferta.getFeirante().getId())
                .distinct()
                .count();
        long encerradas = ofertas.stream()
                .filter(oferta -> oferta.getStatus() == StatusOferta.RETIRADA
                        || oferta.getStatus() == StatusOferta.EXPIRADA)
                .count();
        double taxaDeRetirada = encerradas == 0 ? 0 : arredondar(100.0 * retiradas.size() / encerradas);

        OptionalDouble tempoMedio = ofertas.stream()
                .flatMap(oferta -> oferta.getReserva().stream()
                        .map(reserva -> minutosAteReserva(oferta, reserva)))
                .mapToLong(Long::longValue)
                .average();

        return new PainelView(
                somarPeso(retiradas),
                retiradas.size(),
                cozinhasAtendidas,
                feirantesDoadores,
                ofertas.size(),
                taxaDeRetirada,
                tempoMedio.isPresent() ? arredondar(tempoMedio.getAsDouble()) : null,
                indicadoresPorFeira(ofertas)
        );
    }

    private List<PainelView.IndicadorFeira> indicadoresPorFeira(List<Oferta> ofertas) {
        Map<Long, List<Oferta>> porFeira = ofertas.stream()
                .collect(Collectors.groupingBy(oferta -> oferta.getFeira().getId(), LinkedHashMap::new,
                        Collectors.toList()));
        return porFeira.values().stream()
                .map(this::indicadorDaFeira)
                .sorted(Comparator.comparing(PainelView.IndicadorFeira::quilosRecuperados).reversed()
                        .thenComparing(PainelView.IndicadorFeira::feiraNome))
                .toList();
    }

    private PainelView.IndicadorFeira indicadorDaFeira(List<Oferta> ofertasDaFeira) {
        Feira feira = ofertasDaFeira.getFirst().getFeira();
        List<Oferta> retiradas = ofertasDaFeira.stream()
                .filter(oferta -> oferta.getStatus() == StatusOferta.RETIRADA)
                .toList();
        return new PainelView.IndicadorFeira(
                feira.getId(),
                feira.getNome(),
                somarPeso(retiradas),
                retiradas.size(),
                ofertasDaFeira.size()
        );
    }

    private static BigDecimal somarPeso(List<Oferta> retiradas) {
        return retiradas.stream()
                .map(oferta -> oferta.pesoRetirado().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static long minutosAteReserva(Oferta oferta, Reserva reserva) {
        return Duration.between(oferta.getCriadaEm(), reserva.getReservadaEm()).toMinutes();
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
