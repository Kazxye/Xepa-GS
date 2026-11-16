package br.com.xepasolidaria.domain.cozinha;

import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.geo.Localizacao;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import br.com.xepasolidaria.domain.usuario.Perfil;
import br.com.xepasolidaria.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "cozinha")
public class Cozinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "responsavel", nullable = false, length = 120)
    private String responsavel;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "endereco", nullable = false, length = 200)
    private String endereco;

    @Embedded
    private Localizacao localizacao;

    @Column(name = "raio_busca_km", nullable = false)
    private double raioBuscaKm;

    @Column(name = "capacidade_carga_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadeCargaKg;

    protected Cozinha() {
    }

    public Cozinha(Usuario usuario, String nome, String responsavel, String telefone, String endereco,
                   Localizacao localizacao, double raioBuscaKm, BigDecimal capacidadeCargaKg) {
        if (!usuario.possuiPerfil(Perfil.COZINHA)) {
            throw new RegraDeNegocioException("O usuário precisa ter o perfil de cozinha.");
        }
        if (raioBuscaKm <= 0) {
            throw new RegraDeNegocioException("O raio de busca deve ser maior que zero.");
        }
        if (capacidadeCargaKg == null || capacidadeCargaKg.signum() <= 0) {
            throw new RegraDeNegocioException("A capacidade de carga deve ser maior que zero.");
        }
        this.usuario = usuario;
        this.nome = Objects.requireNonNull(nome).trim();
        this.responsavel = Objects.requireNonNull(responsavel).trim();
        this.telefone = Objects.requireNonNull(telefone).trim();
        this.endereco = Objects.requireNonNull(endereco).trim();
        this.localizacao = Objects.requireNonNull(localizacao);
        this.raioBuscaKm = raioBuscaKm;
        this.capacidadeCargaKg = capacidadeCargaKg;
    }

    public double distanciaAte(Feira feira) {
        return localizacao.distanciaKm(feira.getLocalizacao());
    }

    public boolean alcanca(Feira feira) {
        return distanciaAte(feira) <= raioBuscaKm;
    }

    public boolean mesmaQue(Cozinha outra) {
        return outra != null && id != null && id.equals(outra.getId());
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public double getRaioBuscaKm() {
        return raioBuscaKm;
    }

    public BigDecimal getCapacidadeCargaKg() {
        return capacidadeCargaKg;
    }
}
