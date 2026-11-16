package br.com.xepasolidaria.infrastructure.persistence;

import br.com.xepasolidaria.domain.oferta.Oferta;
import br.com.xepasolidaria.domain.oferta.OfertaRepository;
import br.com.xepasolidaria.domain.oferta.StatusOferta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

interface OfertaJpaRepository extends Repository<Oferta, Long>, OfertaRepository {

    @Override
    @EntityGraph(attributePaths = {"feirante.feira", "reserva.cozinha"})
    Optional<Oferta> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"feirante.feira", "reserva.cozinha"})
    List<Oferta> findAll();

    @Override
    @EntityGraph(attributePaths = {"feirante.feira"})
    List<Oferta> findByStatus(StatusOferta status);

    @Override
    @EntityGraph(attributePaths = {"feirante.feira", "reserva.cozinha"})
    List<Oferta> findByFeiranteIdOrderByCriadaEmDesc(Long feiranteId);

    @Override
    @EntityGraph(attributePaths = {"feirante.feira", "reserva.cozinha"})
    List<Oferta> findByReservaCozinhaIdOrderByHorarioLimiteDesc(Long cozinhaId);
}
