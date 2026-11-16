package br.com.xepasolidaria.infrastructure.persistence;

import br.com.xepasolidaria.domain.usuario.Usuario;
import br.com.xepasolidaria.domain.usuario.UsuarioRepository;
import org.springframework.data.repository.Repository;

interface UsuarioJpaRepository extends Repository<Usuario, Long>, UsuarioRepository {
}
