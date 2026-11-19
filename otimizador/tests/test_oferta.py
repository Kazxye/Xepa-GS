from datetime import datetime, timedelta
from decimal import Decimal

import pytest

from fabricas import criar_oferta
from xepa_otimizador.oferta import Oferta, TipoAlimento, kg_para_unidades


@pytest.mark.parametrize(
    ("prazo", "urgencia"),
    [
        (timedelta(minutes=30), 4),
        (timedelta(hours=1), 4),
        (timedelta(hours=2), 3),
        (timedelta(hours=3), 3),
        (timedelta(hours=5), 2),
        (timedelta(hours=10), 1),
    ],
)
def test_urgencia_cresce_conforme_o_prazo_se_aproxima(agora, prazo, urgencia):
    assert criar_oferta(prazo=prazo).urgencia(agora) == urgencia


def test_valor_combina_peso_perecibilidade_e_urgencia(agora):
    oferta = criar_oferta(tipo=TipoAlimento.VERDURA, quantidade_kg="2.5", prazo=timedelta(minutes=45))

    assert oferta.peso_em_unidades == 250
    assert oferta.valor(agora) == 250 * 3 * 4


def test_verdura_vale_mais_que_fruta_e_fruta_mais_que_legume(agora):
    valores = [criar_oferta(tipo=tipo).valor(agora) for tipo in (TipoAlimento.VERDURA, TipoAlimento.FRUTA, TipoAlimento.LEGUME)]

    assert valores == sorted(valores, reverse=True)
    assert len(set(valores)) == 3


def test_oferta_com_prazo_vencido_nao_esta_disponivel(agora):
    assert not criar_oferta(prazo=timedelta(0)).disponivel_em(agora)
    assert criar_oferta(prazo=timedelta(minutes=1)).disponivel_em(agora)


@pytest.mark.parametrize("quantidade", ["0", "-1", "1000.01", "1.005"])
def test_quantidade_invalida_e_rejeitada(quantidade):
    with pytest.raises(ValueError):
        criar_oferta(quantidade_kg=quantidade)


def test_horario_sem_fuso_e_rejeitado():
    with pytest.raises(ValueError):
        Oferta(1, TipoAlimento.FRUTA, "Banana", Decimal("1"), datetime(2026, 11, 16, 13, 0), "Feira")


def test_conversao_de_kg_para_unidades():
    assert kg_para_unidades(Decimal("18.5")) == 1850
    assert kg_para_unidades(Decimal("0")) == 0
    with pytest.raises(ValueError):
        kg_para_unidades(Decimal("0.001"))
