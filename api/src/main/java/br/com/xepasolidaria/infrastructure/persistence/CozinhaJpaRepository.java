package br.com.xepasolidaria.infrastructure.persistence;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.cozinha.CozinhaRepository;
import org.springframework.data.repository.Repository;

interface CozinhaJpaRepository extends Repository<Cozinha, Long>, CozinhaRepository {
}
