package br.com.xepasolidaria.application.auth;

import br.com.xepasolidaria.application.cozinha.CozinhaView;
import br.com.xepasolidaria.application.feirante.FeiranteView;
import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.cozinha.CozinhaRepository;
import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.feira.FeiraRepository;
import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.feirante.FeiranteRepository;
import br.com.xepasolidaria.domain.geo.Localizacao;
import br.com.xepasolidaria.domain.shared.ConflitoException;
import br.com.xepasolidaria.domain.shared.RecursoNaoEncontradoException;
import br.com.xepasolidaria.domain.usuario.Perfil;
import br.com.xepasolidaria.domain.usuario.Usuario;
import br.com.xepasolidaria.domain.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final FeiranteRepository feiranteRepository;
    private final CozinhaRepository cozinhaRepository;
    private final FeiraRepository feiraRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               FeiranteRepository feiranteRepository,
                               CozinhaRepository cozinhaRepository,
                               FeiraRepository feiraRepository,
                               PasswordEncoder passwordEncoder,
                               TokenService tokenService,
                               Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.feiranteRepository = feiranteRepository;
        this.cozinhaRepository = cozinhaRepository;
        this.feiraRepository = feiraRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public SessaoView cadastrarFeirante(CadastroFeiranteCommand comando) {
        Feira feira = feiraRepository.findById(comando.feiraId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Feira não encontrada."));
        Usuario usuario = criarUsuario(comando.email(), comando.senha(), Perfil.FEIRANTE);
        Feirante feirante = feiranteRepository.save(
                new Feirante(usuario, feira, comando.nome(), comando.telefone(), comando.nomeBarraca()));
        return abrirSessao(usuario, perfilDoFeirante(usuario, feirante));
    }

    @Transactional
    public SessaoView cadastrarCozinha(CadastroCozinhaCommand comando) {
        Usuario usuario = criarUsuario(comando.email(), comando.senha(), Perfil.COZINHA);
        Cozinha cozinha = cozinhaRepository.save(new Cozinha(
                usuario,
                comando.nome(),
                comando.responsavel(),
                comando.telefone(),
                comando.endereco(),
                new Localizacao(comando.latitude(), comando.longitude()),
                comando.raioBuscaKm(),
                comando.capacidadeCargaKg()));
        return abrirSessao(usuario, perfilDaCozinha(usuario, cozinha));
    }

    @Transactional(readOnly = true)
    public SessaoView entrar(LoginCommand comando) {
        Usuario usuario = usuarioRepository.findByEmail(Usuario.normalizarEmail(comando.email()))
                .filter(encontrado -> passwordEncoder.matches(comando.senha(), encontrado.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return abrirSessao(usuario, montarPerfil(usuario));
    }

    @Transactional(readOnly = true)
    public PerfilView perfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return montarPerfil(usuario);
    }

    private Usuario criarUsuario(String email, String senha, Perfil perfil) {
        String emailNormalizado = Usuario.normalizarEmail(email);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflitoException("Já existe uma conta com este e-mail.");
        }
        return usuarioRepository.save(
                new Usuario(emailNormalizado, passwordEncoder.encode(senha), perfil, clock.instant()));
    }

    private SessaoView abrirSessao(Usuario usuario, PerfilView perfil) {
        TokenEmitido token = tokenService.emitir(usuario);
        return new SessaoView(token.valor(), token.expiraEm(), perfil);
    }

    private PerfilView montarPerfil(Usuario usuario) {
        return switch (usuario.getPerfil()) {
            case FEIRANTE -> perfilDoFeirante(usuario, feiranteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de feirante não encontrado.")));
            case COZINHA -> perfilDaCozinha(usuario, cozinhaRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de cozinha não encontrado.")));
        };
    }

    private PerfilView perfilDoFeirante(Usuario usuario, Feirante feirante) {
        return new PerfilView(usuario.getId(), usuario.getEmail(), usuario.getPerfil(), feirante.getNome(),
                FeiranteView.de(feirante), null);
    }

    private PerfilView perfilDaCozinha(Usuario usuario, Cozinha cozinha) {
        return new PerfilView(usuario.getId(), usuario.getEmail(), usuario.getPerfil(), cozinha.getNome(),
                null, CozinhaView.de(cozinha));
    }
}
