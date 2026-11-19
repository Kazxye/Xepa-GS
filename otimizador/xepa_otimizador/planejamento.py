from collections.abc import Iterable
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from enum import StrEnum

from xepa_otimizador.mochila import (
    Item,
    Resolvedor,
    resolver_por_forca_bruta,
    resolver_por_memoizacao,
    resolver_por_tabulacao,
)
from xepa_otimizador.oferta import Oferta, kg_para_unidades


class Metodo(StrEnum):
    TABULACAO = "tabulacao"
    MEMOIZACAO = "memoizacao"
    FORCA_BRUTA = "forca-bruta"

    @property
    def rotulo(self) -> str:
        return ROTULOS[self]

    @property
    def resolvedor(self) -> Resolvedor:
        return RESOLVEDORES[self]


ROTULOS = {
    Metodo.TABULACAO: "tabulação",
    Metodo.MEMOIZACAO: "memoização",
    Metodo.FORCA_BRUTA: "força bruta",
}

RESOLVEDORES: dict[Metodo, Resolvedor] = {
    Metodo.TABULACAO: resolver_por_tabulacao,
    Metodo.MEMOIZACAO: resolver_por_memoizacao,
    Metodo.FORCA_BRUTA: resolver_por_forca_bruta,
}


@dataclass(frozen=True, slots=True)
class OfertaAvaliada:
    oferta: Oferta
    urgencia: int
    valor: int

    @classmethod
    def avaliar(cls, oferta: Oferta, agora: datetime) -> "OfertaAvaliada":
        return cls(oferta=oferta, urgencia=oferta.urgencia(agora), valor=oferta.valor(agora))

    def como_item(self) -> Item:
        return Item(peso=self.oferta.peso_em_unidades, valor=self.valor)


@dataclass(frozen=True, slots=True)
class PlanoDeColeta:
    capacidade_kg: Decimal
    metodo: Metodo
    escolhidas: tuple[OfertaAvaliada, ...]
    deixadas: tuple[OfertaAvaliada, ...]
    vencidas: tuple[Oferta, ...]
    subproblemas: int

    @property
    def total_kg(self) -> Decimal:
        return sum((avaliada.oferta.quantidade_kg for avaliada in self.escolhidas), Decimal("0"))

    @property
    def carga_livre_kg(self) -> Decimal:
        return self.capacidade_kg - self.total_kg

    @property
    def valor_total(self) -> int:
        return sum(avaliada.valor for avaliada in self.escolhidas)


def avaliar_disponiveis(ofertas: Iterable[Oferta], agora: datetime) -> tuple[OfertaAvaliada, ...]:
    return tuple(OfertaAvaliada.avaliar(oferta, agora) for oferta in ofertas if oferta.disponivel_em(agora))


def planejar_coleta(
    ofertas: Iterable[Oferta],
    capacidade_kg: Decimal,
    agora: datetime,
    metodo: Metodo = Metodo.TABULACAO,
) -> PlanoDeColeta:
    if capacidade_kg < 0:
        raise ValueError("A capacidade de carga não pode ser negativa.")
    ofertas = tuple(ofertas)
    avaliadas = avaliar_disponiveis(ofertas, agora)
    solucao = metodo.resolvedor([avaliada.como_item() for avaliada in avaliadas], kg_para_unidades(capacidade_kg))
    escolhidos = set(solucao.indices)
    return PlanoDeColeta(
        capacidade_kg=capacidade_kg,
        metodo=metodo,
        escolhidas=tuple(avaliada for i, avaliada in enumerate(avaliadas) if i in escolhidos),
        deixadas=tuple(avaliada for i, avaliada in enumerate(avaliadas) if i not in escolhidos),
        vencidas=tuple(oferta for oferta in ofertas if not oferta.disponivel_em(agora)),
        subproblemas=solucao.subproblemas,
    )
