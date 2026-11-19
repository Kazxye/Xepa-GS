from collections.abc import Sequence
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from math import ceil

from xepa_otimizador.mochila import TabelaDP
from xepa_otimizador.oferta import UNIDADES_POR_KG, Oferta
from xepa_otimizador.planejamento import Metodo, OfertaAvaliada, PlanoDeColeta

COLUNAS_MAXIMAS_DA_TABELA = 12


@dataclass(frozen=True, slots=True)
class Medicao:
    metodo: Metodo
    valor: int
    subproblemas: int
    milissegundos: float


def kg(quantidade: Decimal) -> str:
    return f"{quantidade:.2f}".replace(".", ",")


def inteiro(numero: int) -> str:
    return f"{numero:,}".replace(",", ".")


def data_hora(momento: datetime, fuso: datetime) -> str:
    return momento.astimezone(fuso.tzinfo).strftime("%d/%m %H:%M")


def tabela(cabecalho: Sequence[str], linhas: Sequence[Sequence[str]], numericas: frozenset[int] = frozenset()) -> str:
    larguras = [max(len(texto) for texto in coluna) for coluna in zip(cabecalho, *linhas)]

    def formatar(linha: Sequence[str]) -> str:
        celulas = (
            texto.rjust(largura) if posicao in numericas else texto.ljust(largura)
            for posicao, (texto, largura) in enumerate(zip(linha, larguras))
        )
        return "  ".join(celulas).rstrip()

    separador = "  ".join("-" * largura for largura in larguras)
    return "\n".join([formatar(cabecalho), separador, *(formatar(linha) for linha in linhas)])


def plano(plano_de_coleta: PlanoDeColeta, agora: datetime) -> str:
    partes = [
        f"Plano de coleta por {plano_de_coleta.metodo.rotulo}",
        f"Capacidade {kg(plano_de_coleta.capacidade_kg)} kg, referência {agora.strftime('%d/%m/%Y %H:%M')}",
        "",
        "Levar",
        _ofertas_avaliadas(plano_de_coleta.escolhidas, agora),
        "",
        f"Carga: {kg(plano_de_coleta.total_kg)} de {kg(plano_de_coleta.capacidade_kg)} kg"
        f" ({kg(plano_de_coleta.carga_livre_kg)} kg livres)",
        f"Valor total: {inteiro(plano_de_coleta.valor_total)}",
        f"Subproblemas resolvidos: {inteiro(plano_de_coleta.subproblemas)}",
        "",
        "Ficam para outra cozinha",
        _ofertas_avaliadas(plano_de_coleta.deixadas, agora),
        "",
        "Vencidas",
        _ofertas_vencidas(plano_de_coleta.vencidas, agora),
    ]
    return "\n".join(partes)


def tabela_dp(dp: TabelaDP, avaliadas: Sequence[OfertaAvaliada]) -> str:
    colunas = _colunas_amostradas(dp.capacidade)
    cabecalho = ["Ofertas consideradas", *(f"{kg(Decimal(c) / UNIDADES_POR_KG)} kg" for c in colunas)]
    rotulos = ["nenhuma", *(f"até #{avaliada.oferta.id} ({kg(avaliada.oferta.quantidade_kg)} kg)" for avaliada in avaliadas)]
    linhas = [
        [rotulo, *(inteiro(dp.valor(i, c)) for c in colunas)]
        for i, rotulo in enumerate(rotulos)
    ]
    numericas = frozenset(range(1, len(cabecalho)))
    return "\n".join(["Tabela de programação dinâmica (valor máximo por capacidade)", tabela(cabecalho, linhas, numericas)])


def comparacao(medicoes: Sequence[Medicao], ignorados: Sequence[Metodo] = ()) -> str:
    linhas = [
        [medicao.metodo.rotulo, inteiro(medicao.valor), inteiro(medicao.subproblemas), f"{medicao.milissegundos:.2f}"]
        for medicao in medicoes
    ]
    partes = [
        "Comparação entre métodos",
        tabela(["Método", "Valor", "Subproblemas", "Tempo (ms)"], linhas, frozenset({1, 2, 3})),
    ]
    partes.extend(f"{metodo.rotulo.capitalize()} ignorada: ofertas demais para testar todas as combinações." for metodo in ignorados)
    return "\n".join(partes)


def _ofertas_avaliadas(avaliadas: Sequence[OfertaAvaliada], agora: datetime) -> str:
    if not avaliadas:
        return "Nenhuma."
    linhas = [
        [
            f"#{avaliada.oferta.id}",
            avaliada.oferta.tipo.nome,
            avaliada.oferta.descricao,
            avaliada.oferta.feira,
            kg(avaliada.oferta.quantidade_kg),
            data_hora(avaliada.oferta.horario_limite, agora),
            str(avaliada.urgencia),
            inteiro(avaliada.valor),
        ]
        for avaliada in avaliadas
    ]
    cabecalho = ["Oferta", "Alimento", "Descrição", "Feira", "Kg", "Prazo", "Urgência", "Valor"]
    return tabela(cabecalho, linhas, frozenset({4, 6, 7}))


def _ofertas_vencidas(ofertas: Sequence[Oferta], agora: datetime) -> str:
    if not ofertas:
        return "Nenhuma."
    linhas = [
        [f"#{oferta.id}", oferta.tipo.nome, oferta.descricao, kg(oferta.quantidade_kg), data_hora(oferta.horario_limite, agora)]
        for oferta in ofertas
    ]
    return tabela(["Oferta", "Alimento", "Descrição", "Kg", "Prazo"], linhas, frozenset({3}))


def _colunas_amostradas(capacidade: int) -> list[int]:
    passo = max(UNIDADES_POR_KG, ceil(capacidade / COLUNAS_MAXIMAS_DA_TABELA / UNIDADES_POR_KG) * UNIDADES_POR_KG)
    colunas = list(range(0, capacidade + 1, passo))
    if colunas[-1] != capacidade:
        colunas.append(capacidade)
    return colunas
