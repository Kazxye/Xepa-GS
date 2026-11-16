package br.com.xepasolidaria.domain.oferta;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import jakarta.persistence.CascadeType;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false, unique = true)
    private Oferta oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cozinha_id", nullable = false)
    private Cozinha cozinha;

    @Column(name = "reservada_em", nullable = false)
    private Instant reservadaEm;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private Retirada retirada;

    protected Reserva() {
    }

    Reserva(Oferta oferta, Cozinha cozinha, Instant reservadaEm) {
        this.oferta = oferta;
        this.cozinha = cozinha;
        this.reservadaEm = reservadaEm;
    }

    void confirmarPeloFeirante(Instant agora) {
        retiradaEmAndamento().confirmarPeloFeirante(agora);
    }

    void confirmarPelaCozinha(BigDecimal pesoKg, Instant agora) {
        retiradaEmAndamento().confirmarPelaCozinha(pesoKg, agora);
    }

    boolean retiradaConcluida() {
        return retirada != null && retirada.isConcluida();
    }

    private Retirada retiradaEmAndamento() {
        if (retirada == null) {
            retirada = new Retirada(this);
        }
        return retirada;
    }

    public boolean pertenceA(Cozinha outra) {
        return cozinha.mesmaQue(outra);
    }

    public Long getId() {
        return id;
    }

    public Cozinha getCozinha() {
        return cozinha;
    }

    public Instant getReservadaEm() {
        return reservadaEm;
    }

    public Optional<Retirada> getRetirada() {
        return Optional.ofNullable(retirada);
    }
}
