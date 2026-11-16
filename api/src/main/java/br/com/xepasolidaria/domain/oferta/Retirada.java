package br.com.xepasolidaria.domain.oferta;

import br.com.xepasolidaria.domain.shared.ConflitoException;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "retirada")
public class Retirada {

    static final BigDecimal PESO_MAXIMO_KG = new BigDecimal("1000");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "peso_kg", precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "confirmado_feirante_em")
    private Instant confirmadoFeiranteEm;

    @Column(name = "confirmado_cozinha_em")
    private Instant confirmadoCozinhaEm;

    @Column(name = "concluida_em")
    private Instant concluidaEm;

    protected Retirada() {
    }

    Retirada(Reserva reserva) {
        this.reserva = reserva;
    }

    void confirmarPeloFeirante(Instant agora) {
        if (confirmadoFeiranteEm != null) {
            throw new ConflitoException("O feirante já confirmou esta retirada.");
        }
        confirmadoFeiranteEm = agora;
        concluirSeConfirmada(agora);
    }

    void confirmarPelaCozinha(BigDecimal pesoKg, Instant agora) {
        if (confirmadoCozinhaEm != null) {
            throw new ConflitoException("A cozinha já confirmou esta retirada.");
        }
        if (pesoKg == null || pesoKg.signum() <= 0 || pesoKg.compareTo(PESO_MAXIMO_KG) > 0) {
            throw new RegraDeNegocioException("O peso retirado deve ser maior que zero e no máximo 1000 kg.");
        }
        this.pesoKg = pesoKg;
        confirmadoCozinhaEm = agora;
        concluirSeConfirmada(agora);
    }

    private void concluirSeConfirmada(Instant agora) {
        if (confirmadoFeiranteEm != null && confirmadoCozinhaEm != null) {
            concluidaEm = agora;
        }
    }

    public boolean isConcluida() {
        return concluidaEm != null;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public Instant getConfirmadoFeiranteEm() {
        return confirmadoFeiranteEm;
    }

    public Instant getConfirmadoCozinhaEm() {
        return confirmadoCozinhaEm;
    }

    public Instant getConcluidaEm() {
        return concluidaEm;
    }
}
