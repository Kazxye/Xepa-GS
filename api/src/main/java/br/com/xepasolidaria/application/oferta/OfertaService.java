package br.com.xepasolidaria.application.oferta;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.cozinha.CozinhaRepository;
import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.feirante.FeiranteRepository;
import br.com.xepasolidaria.domain.oferta.Oferta;
import br.com.xepasolidaria.domain.oferta.OfertaRepository;
import br.com.xepasolidaria.domain.oferta.StatusOferta;
import br.com.xepasolidaria.domain.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class OfertaService {

    private static final List<StatusOferta> STATUS_QUE_EXPIRAM = List.of(StatusOferta.DISPONIVEL, StatusOferta.RESERVADA);

    private final OfertaRepository ofertaRepository;
    private final FeiranteRepository feiranteRepository;
    private final CozinhaRepository cozinhaRepository;
    private final Clock clock;

    public OfertaService(OfertaRepository ofertaRepository,
                         FeiranteRepository feiranteRepository,
                         CozinhaRepository cozinhaRepository,
                         Clock clock) {
        this.ofertaRepository = ofertaRepository;
        this.feiranteRepository = feiranteRepository;
        this.cozinhaRepository = cozinhaRepository;
        this.clock = clock;
    }

    @Transactional
    public OfertaView publicar(Long usuarioId, PublicarOfertaCommand comando) {
        Feirante feirante = feirante(usuarioId);
        Oferta oferta = Oferta.publicar(
                feirante,
                comando.tipo(),
                comando.descricao(),
                comando.quantidadeKg(),
                comando.horarioLimite(),
                comando.fotoUrl(),
                clock.instant());
        return OfertaView.de(ofertaRepository.save(oferta));
    }

    @Transactional(readOnly = true)
    public List<OfertaView> listarDoFeirante(Long usuarioId) {
        Feirante feirante = feirante(usuarioId);
        return ofertaRepository.findByFeiranteIdOrderByCriadaEmDesc(feirante.getId()).stream()
                .map(OfertaView::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaView> listarProximas(Long usuarioId) {
        Cozinha cozinha = cozinha(usuarioId);
        Instant agora = clock.instant();
        return ofertaRepository.findByStatus(StatusOferta.DISPONIVEL).stream()
                .filter(oferta -> oferta.estaDisponivelEm(agora))
                .filter(oferta -> cozinha.alcanca(oferta.getFeira()))
                .map(oferta -> OfertaView.de(oferta, arredondar(cozinha.distanciaAte(oferta.getFeira()))))
                .sorted(Comparator.comparing(OfertaView::distanciaKm).thenComparing(OfertaView::horarioLimite))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaView> listarReservasDaCozinha(Long usuarioId) {
        Cozinha cozinha = cozinha(usuarioId);
        return ofertaRepository.findByReservaCozinhaIdOrderByHorarioLimiteDesc(cozinha.getId()).stream()
                .map(oferta -> OfertaView.de(oferta, arredondar(cozinha.distanciaAte(oferta.getFeira()))))
                .toList();
    }

    @Transactional
    public OfertaView reservar(Long usuarioId, Long ofertaId) {
        Cozinha cozinha = cozinha(usuarioId);
        Oferta oferta = oferta(ofertaId);
        oferta.reservar(cozinha, clock.instant());
        return OfertaView.de(ofertaRepository.save(oferta), arredondar(cozinha.distanciaAte(oferta.getFeira())));
    }

    @Transactional
    public OfertaView confirmarEntregaPeloFeirante(Long usuarioId, Long ofertaId) {
        Feirante feirante = feirante(usuarioId);
        Oferta oferta = oferta(ofertaId);
        oferta.confirmarRetiradaPeloFeirante(feirante, clock.instant());
        return OfertaView.de(ofertaRepository.save(oferta));
    }

    @Transactional
    public OfertaView confirmarRetiradaPelaCozinha(Long usuarioId, Long ofertaId, ConfirmarRetiradaCommand comando) {
        Cozinha cozinha = cozinha(usuarioId);
        Oferta oferta = oferta(ofertaId);
        oferta.confirmarRetiradaPelaCozinha(cozinha, comando.pesoKg(), clock.instant());
        return OfertaView.de(ofertaRepository.save(oferta), arredondar(cozinha.distanciaAte(oferta.getFeira())));
    }

    @Transactional
    public int expirarVencidas() {
        Instant agora = clock.instant();
        int expiradas = 0;
        for (Oferta oferta : ofertaRepository.findByStatusInAndHorarioLimiteBefore(STATUS_QUE_EXPIRAM, agora)) {
            if (oferta.expirarSeVencida(agora)) {
                expiradas++;
            }
        }
        return expiradas;
    }

    private Feirante feirante(Long usuarioId) {
        return feiranteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de feirante não encontrado."));
    }

    private Cozinha cozinha(Long usuarioId) {
        return cozinhaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de cozinha não encontrado."));
    }

    private Oferta oferta(Long ofertaId) {
        return ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oferta não encontrada."));
    }

    private static double arredondar(double distanciaKm) {
        return Math.round(distanciaKm * 100) / 100.0;
    }
}
