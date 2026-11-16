package br.com.xepasolidaria.infrastructure.persistence;

import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.feira.FeiraRepository;
import org.springframework.data.repository.Repository;

interface FeiraJpaRepository extends Repository<Feira, Long>, FeiraRepository {
}
