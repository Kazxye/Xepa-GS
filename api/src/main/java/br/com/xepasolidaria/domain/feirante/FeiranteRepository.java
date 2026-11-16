package br.com.xepasolidaria.domain.feirante;

import java.util.Optional;

public interface FeiranteRepository {

    Feirante save(Feirante feirante);

    Optional<Feirante> findByUsuarioId(Long usuarioId);
}
