package br.com.xepasolidaria.domain.feira;

import java.util.List;
import java.util.Optional;

public interface FeiraRepository {

    Optional<Feira> findById(Long id);

    List<Feira> findAllByOrderByNomeAsc();
}
