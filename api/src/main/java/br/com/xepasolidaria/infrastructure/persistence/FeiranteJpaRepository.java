package br.com.xepasolidaria.infrastructure.persistence;

import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.feirante.FeiranteRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.Optional;

interface FeiranteJpaRepository extends Repository<Feirante, Long>, FeiranteRepository {

    @Override
    @EntityGraph(attributePaths = {"feira", "usuario"})
    Optional<Feirante> findByUsuarioId(Long usuarioId);
}
