package br.com.xepasolidaria.domain.oferta;

import br.com.xepasolidaria.domain.cozinha.Cozinha;
import br.com.xepasolidaria.domain.feira.Feira;
import br.com.xepasolidaria.domain.feirante.Feirante;
import br.com.xepasolidaria.domain.shared.ConflitoException;
import br.com.xepasolidaria.domain.shared.OperacaoNaoPermitidaException;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "oferta")
public class Oferta {

    public static final Duration PRAZO_MAXIMO_PUBLICACAO = Duration.ofHours(24);
    public static final Duration TOLERANCIA_RETIRADA = Duration.ofHours(1);
    public static final BigDecimal QUANTIDADE_MAXIMA_KG = new BigDecimal("1000");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feirante_id", nullable = false)
    private Feirante feirante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoAlimento tipo;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "quantidade_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeKg;

    @Column(name = "horario_limite", nullable = false)
    private Instant horarioLimite;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusOferta status;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @OneToOne(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reserva reserva;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    protected Oferta() {
    }

    private Oferta(Feirante feirante, TipoAlimento tipo, String descricao, BigDecimal quantidadeKg,
                   Instant horarioLimite, String fotoUrl, Instant criadaEm) {
        this.feirante = feirante;
        this.tipo = tipo;
        this.descricao = descricao;
        this.quantidadeKg = quantidadeKg;
        this.horarioLimite = horarioLimite;
        this.fotoUrl = fotoUrl;
        this.criadaEm = criadaEm;
        this.status = StatusOferta.DISPONIVEL;
    }

    public static Oferta publicar(Feirante feirante, TipoAlimento tipo, String descricao, BigDecimal quantidadeKg,
                                  Instant horarioLimite, String fotoUrl, Instant agora) {
        Objects.requireNonNull(feirante);
        Objects.requireNonNull(tipo);
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("Descreva o alimento oferecido.");
        }
        if (quantidadeKg == null || quantidadeKg.signum() <= 0 || quantidadeKg.compareTo(QUANTIDADE_MAXIMA_KG) > 0) {
            throw new RegraDeNegocioException("A quantidade deve ser maior que zero e no máximo 1000 kg.");
        }
        if (horarioLimite == null || !horarioLimite.isAfter(agora)) {
            throw new RegraDeNegocioException("O horário limite de retirada precisa estar no futuro.");
        }
        if (horarioLimite.isAfter(agora.plus(PRAZO_MAXIMO_PUBLICACAO))) {
            throw new RegraDeNegocioException("O horário limite de retirada deve ser em até 24 horas.");
        }
        String foto = fotoUrl == null || fotoUrl.isBlank() ? null : fotoUrl.trim();
        return new Oferta(feirante, tipo, descricao.trim(), quantidadeKg, horarioLimite, foto, agora);
    }

    public void reservar(Cozinha cozinha, Instant agora) {
        Objects.requireNonNull(cozinha);
        if (reserva != null && reserva.pertenceA(cozinha)) {
            throw new ConflitoException("Sua cozinha já reservou esta oferta.");
        }
        if (status == StatusOferta.RESERVADA || status == StatusOferta.RETIRADA) {
            throw new ConflitoException("Esta oferta já foi reservada por outra cozinha.");
        }
        if (status == StatusOferta.EXPIRADA || !agora.isBefore(horarioLimite)) {
            throw new RegraDeNegocioException("O horário limite de retirada desta oferta já passou.");
        }
        if (!cozinha.alcanca(getFeira())) {
            throw new RegraDeNegocioException("Esta oferta está fora do raio de busca da cozinha.");
        }
        reserva = new Reserva(this, cozinha, agora);
        status = StatusOferta.RESERVADA;
    }

    public void confirmarRetiradaPeloFeirante(Feirante solicitante, Instant agora) {
        if (!feirante.mesmoQue(solicitante)) {
            throw new OperacaoNaoPermitidaException("Somente o feirante que publicou a oferta pode confirmar a entrega.");
        }
        exigirRetiradaEmAndamento(agora);
        reserva.confirmarPeloFeirante(agora);
        concluirSeRetirada();
    }

    public void confirmarRetiradaPelaCozinha(Cozinha solicitante, BigDecimal pesoKg, Instant agora) {
        if (reserva == null || !reserva.pertenceA(solicitante)) {
            throw new OperacaoNaoPermitidaException("Somente a cozinha que reservou a oferta pode confirmar a retirada.");
        }
        exigirRetiradaEmAndamento(agora);
        reserva.confirmarPelaCozinha(pesoKg, agora);
        concluirSeRetirada();
    }

    public boolean expirarSeVencida(Instant agora) {
        boolean disponivelVencida = status == StatusOferta.DISPONIVEL && !agora.isBefore(horarioLimite);
        boolean reservaVencida = status == StatusOferta.RESERVADA && !agora.isBefore(prazoFinalRetirada());
        if (disponivelVencida || reservaVencida) {
            status = StatusOferta.EXPIRADA;
            return true;
        }
        return false;
    }

    public boolean estaDisponivelEm(Instant agora) {
        return status == StatusOferta.DISPONIVEL && agora.isBefore(horarioLimite);
    }

    public Optional<BigDecimal> pesoRetirado() {
        if (status != StatusOferta.RETIRADA) {
            return Optional.empty();
        }
        return reserva.getRetirada().map(Retirada::getPesoKg);
    }

    public Feira getFeira() {
        return feirante.getFeira();
    }

    private void exigirRetiradaEmAndamento(Instant agora) {
        if (status == StatusOferta.RETIRADA) {
            throw new ConflitoException("Esta retirada já foi concluída.");
        }
        if (status != StatusOferta.RESERVADA) {
            throw new RegraDeNegocioException("A oferta precisa estar reservada para confirmar a retirada.");
        }
        if (!agora.isBefore(prazoFinalRetirada())) {
            throw new RegraDeNegocioException("O prazo para confirmar esta retirada terminou.");
        }
    }

    private Instant prazoFinalRetirada() {
        return horarioLimite.plus(TOLERANCIA_RETIRADA);
    }

    private void concluirSeRetirada() {
        if (reserva.retiradaConcluida()) {
            status = StatusOferta.RETIRADA;
        }
    }

    public Long getId() {
        return id;
    }

    public Feirante getFeirante() {
        return feirante;
    }

    public TipoAlimento getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public Instant getHorarioLimite() {
        return horarioLimite;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public StatusOferta getStatus() {
        return status;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Optional<Reserva> getReserva() {
        return Optional.ofNullable(reserva);
    }
}
