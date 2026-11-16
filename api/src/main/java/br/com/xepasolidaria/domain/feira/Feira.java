package br.com.xepasolidaria.domain.feira;

import br.com.xepasolidaria.domain.geo.Localizacao;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.util.Objects;

@Entity
@Table(name = "feira")
public class Feira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "endereco", nullable = false, length = 200)
    private String endereco;

    @Embedded
    private Localizacao localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DayOfWeek diaSemana;

    protected Feira() {
    }

    public Feira(String nome, String endereco, Localizacao localizacao, DayOfWeek diaSemana) {
        this.nome = Objects.requireNonNull(nome);
        this.endereco = Objects.requireNonNull(endereco);
        this.localizacao = Objects.requireNonNull(localizacao);
        this.diaSemana = Objects.requireNonNull(diaSemana);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }
}
