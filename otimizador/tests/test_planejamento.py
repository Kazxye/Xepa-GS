from datetime import timedelta
from decimal import Decimal

import pytest

from fabricas import criar_oferta
from xepa_otimizador.oferta import TipoAlimento
from xepa_otimizador.planejamento import Metodo, planejar_coleta


def test_plano_respeita_a_capacidade_e_separa_as_ofertas(agora):
    ofertas = [
        criar_oferta(1, quantidade_kg="30"),
        criar_oferta(2, quantidade_kg="25"),
        criar_oferta(3, quantidade_kg="20"),
        criar_oferta(4, quantidade_kg="5", prazo=timedelta(minutes=-10)),
    ]

    plano = planejar_coleta(ofertas, Decimal("50"), agora)

    assert [avaliada.oferta.id for avaliada in plano.escolhidas] == [1, 3]
    assert [avaliada.oferta.id for avaliada in plano.deixadas] == [2]
    assert [oferta.id for oferta in plano.vencidas] == [4]
    assert plano.total_kg == Decimal("50")
    assert plano.carga_livre_kg == Decimal("0")


def test_entre_pesos_iguais_prefere_o_alimento_mais_urgente(agora):
    ofertas = [
        criar_oferta(1, tipo=TipoAlimento.LEGUME, prazo=timedelta(hours=8)),
        criar_oferta(2, tipo=TipoAlimento.VERDURA, prazo=timedelta(minutes=40)),
    ]

    plano = planejar_coleta(ofertas, Decimal("10"), agora)

    assert [avaliada.oferta.id for avaliada in plano.escolhidas] == [2]


@pytest.mark.parametrize("metodo", list(Metodo))
def test_todos_os_metodos_chegam_ao_mesmo_valor(agora, metodo):
    ofertas = [criar_oferta(i, quantidade_kg=kg, prazo=timedelta(hours=i)) for i, kg in enumerate(["12", "7.5", "20", "3.25", "15"], 1)]

    referencia = planejar_coleta(ofertas, Decimal("30"), agora)
    plano = planejar_coleta(ofertas, Decimal("30"), agora, metodo)

    assert plano.valor_total == referencia.valor_total
    assert plano.total_kg <= Decimal("30")


def test_capacidade_negativa_e_rejeitada(agora):
    with pytest.raises(ValueError):
        planejar_coleta([criar_oferta()], Decimal("-1"), agora)


def test_sem_ofertas_gera_plano_vazio(agora):
    plano = planejar_coleta([], Decimal("40"), agora)

    assert plano.escolhidas == ()
    assert plano.total_kg == Decimal("0")
    assert plano.valor_total == 0
