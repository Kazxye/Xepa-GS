package br.com.xepasolidaria.domain.oferta;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OfertaRepository {

    Oferta save(Oferta oferta);

    Optional<Oferta> findById(Long id);

    List<Oferta> findAll();

    List<Oferta> findByStatus(StatusOferta status);

    List<Oferta> findByFeiranteIdOrderByCriadaEmDesc(Long feiranteId);

    List<Oferta> findByReservaCozinhaIdOrderByHorarioLimiteDesc(Long cozinhaId);

    List<Oferta> findByStatusInAndHorarioLimiteBefore(Collection<StatusOferta> status, Instant limite);
}
