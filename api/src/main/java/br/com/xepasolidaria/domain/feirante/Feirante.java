package br.com.xepasolidaria.domain.feirante;

import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import br.com.xepasolidaria.domain.usuario.Perfil;
import br.com.xepasolidaria.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "feirante")
public class Feirante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feira_id", nullable = false)
    private Feira feira;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "nome_barraca", nullable = false, length = 120)
    private String nomeBarraca;

    protected Feirante() {
    }

    public Feirante(Usuario usuario, Feira feira, String nome, String telefone, String nomeBarraca) {
        if (!usuario.possuiPerfil(Perfil.FEIRANTE)) {
            throw new RegraDeNegocioException("O usuário precisa ter o perfil de feirante.");
        }
        this.usuario = usuario;
        this.feira = Objects.requireNonNull(feira);
        this.nome = Objects.requireNonNull(nome).trim();
        this.telefone = Objects.requireNonNull(telefone).trim();
        this.nomeBarraca = Objects.requireNonNull(nomeBarraca).trim();
    }

    public boolean mesmoQue(Feirante outro) {
        return outro != null && id != null && id.equals(outro.getId());
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Feira getFeira() {
        return feira;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getNomeBarraca() {
        return nomeBarraca;
    }
}
