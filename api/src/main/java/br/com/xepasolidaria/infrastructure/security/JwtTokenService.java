package br.com.xepasolidaria.infrastructure.security;

import br.com.xepasolidaria.application.auth.TokenEmitido;
import br.com.xepasolidaria.application.auth.TokenService;
import br.com.xepasolidaria.domain.usuario.Usuario;
import br.com.xepasolidaria.infrastructure.config.XepaProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenService implements TokenService {

    static final String EMISSOR = "xepa-solidaria";
    static final String CLAIM_PERFIL = "perfil";

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration validade;

    public JwtTokenService(JwtEncoder jwtEncoder, Clock clock, XepaProperties propriedades) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.validade = propriedades.jwt().validade();
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = clock.instant();
        Instant expiraEm = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .claim("email", usuario.getEmail())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenEmitido(token, expiraEm);
    }
}
