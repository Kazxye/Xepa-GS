package br.com.xepasolidaria.domain.cozinha;

import java.util.Optional;

public interface CozinhaRepository {

    Cozinha save(Cozinha cozinha);

    Optional<Cozinha> findByUsuarioId(Long usuarioId);
}
