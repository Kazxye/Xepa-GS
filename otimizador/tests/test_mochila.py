import random

import pytest

from xepa_otimizador.mochila import (
    Item,
    TabelaDP,
    resolver_por_forca_bruta,
    resolver_por_memoizacao,
    resolver_por_tabulacao,
)

RESOLVEDORES = [resolver_por_tabulacao, resolver_por_memoizacao, resolver_por_forca_bruta]

ITENS_CLASSICOS = [Item(1, 1), Item(3, 4), Item(4, 5), Item(5, 7)]


@pytest.mark.parametrize("resolver", RESOLVEDORES)
def test_encontra_a_melhor_combinacao_do_exemplo_classico(resolver):
    solucao = resolver(ITENS_CLASSICOS, 7)

    assert solucao.valor == 9
    assert solucao.peso == 7
    assert solucao.indices == (1, 2)


@pytest.mark.parametrize("resolver", RESOLVEDORES)
def test_capacidade_zero_nao_escolhe_nada(resolver):
    solucao = resolver(ITENS_CLASSICOS, 0)

    assert solucao.indices == ()
    assert solucao.valor == 0


@pytest.mark.parametrize("resolver", RESOLVEDORES)
def test_lista_vazia_nao_escolhe_nada(resolver):
    assert resolver([], 10).indices == ()


@pytest.mark.parametrize("resolver", RESOLVEDORES)
def test_item_maior_que_a_capacidade_fica_de_fora(resolver):
    solucao = resolver([Item(11, 100), Item(4, 3)], 10)

    assert solucao.indices == (1,)


@pytest.mark.parametrize("resolver", RESOLVEDORES)
def test_capacidade_negativa_e_rejeitada(resolver):
    with pytest.raises(ValueError):
        resolver(ITENS_CLASSICOS, -1)


@pytest.mark.parametrize(("peso", "valor"), [(0, 1), (-2, 1), (1, -1)])
def test_item_invalido_e_rejeitado(peso, valor):
    with pytest.raises(ValueError):
        Item(peso, valor)


def test_programacao_dinamica_supera_a_escolha_gulosa():
    itens = [Item(10, 60), Item(20, 100), Item(30, 120)]

    solucao = resolver_por_tabulacao(itens, 50)

    assert solucao.valor == 220
    assert solucao.indices == (1, 2)


def test_os_tres_metodos_concordam_em_instancias_aleatorias():
    gerador = random.Random(2026)
    for _ in range(300):
        itens = [Item(gerador.randint(1, 15), gerador.randint(0, 40)) for _ in range(gerador.randint(0, 10))]
        capacidade = gerador.randint(0, 40)

        tabulacao = resolver_por_tabulacao(itens, capacidade)
        memoizacao = resolver_por_memoizacao(itens, capacidade)
        forca_bruta = resolver_por_forca_bruta(itens, capacidade)

        assert tabulacao.valor == memoizacao.valor == forca_bruta.valor
        assert tabulacao.indices == memoizacao.indices
        assert tabulacao.peso <= capacidade
        assert tabulacao.valor == sum(itens[i].valor for i in tabulacao.indices)


def test_tabela_guarda_o_valor_maximo_de_cada_subproblema():
    tabela = TabelaDP(ITENS_CLASSICOS, 7)

    assert [tabela.valor(4, c) for c in range(8)] == [0, 1, 1, 4, 5, 7, 8, 9]
    assert tabela.valor(0, 7) == 0
    assert tabela.subproblemas == 5 * 8


def test_memoizacao_resolve_no_maximo_os_subproblemas_da_tabela():
    itens = [Item(peso, peso * 3) for peso in range(1, 13)]

    memoizacao = resolver_por_memoizacao(itens, 30)

    assert memoizacao.subproblemas <= TabelaDP(itens, 30).subproblemas
