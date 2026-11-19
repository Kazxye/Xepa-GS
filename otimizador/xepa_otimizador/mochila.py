from collections.abc import Callable, Iterable, Sequence
from dataclasses import dataclass
from functools import cache
from itertools import combinations


@dataclass(frozen=True, slots=True)
class Item:
    peso: int
    valor: int

    def __post_init__(self) -> None:
        if self.peso <= 0:
            raise ValueError("O peso do item deve ser positivo.")
        if self.valor < 0:
            raise ValueError("O valor do item não pode ser negativo.")


@dataclass(frozen=True, slots=True)
class Solucao:
    indices: tuple[int, ...]
    valor: int
    peso: int
    subproblemas: int


Resolvedor = Callable[[Sequence[Item], int], Solucao]


class TabelaDP:
    def __init__(self, itens: Sequence[Item], capacidade: int) -> None:
        _validar_capacidade(capacidade)
        self._itens = tuple(itens)
        self._capacidade = capacidade
        self._celulas = self._preencher()

    @property
    def itens(self) -> tuple[Item, ...]:
        return self._itens

    @property
    def capacidade(self) -> int:
        return self._capacidade

    @property
    def subproblemas(self) -> int:
        return len(self._celulas) * (self._capacidade + 1)

    def valor(self, quantidade_de_itens: int, capacidade: int) -> int:
        return self._celulas[quantidade_de_itens][capacidade]

    def solucao(self) -> Solucao:
        indices = _reconstruir(self._itens, self._capacidade, self.valor)
        return _montar_solucao(self._itens, indices, self.subproblemas)

    def _preencher(self) -> list[list[int]]:
        celulas = [[0] * (self._capacidade + 1)]
        for item in self._itens:
            anterior = celulas[-1]
            atual = anterior.copy()
            for capacidade in range(item.peso, self._capacidade + 1):
                com_item = anterior[capacidade - item.peso] + item.valor
                atual[capacidade] = max(anterior[capacidade], com_item)
            celulas.append(atual)
        return celulas


def resolver_por_tabulacao(itens: Sequence[Item], capacidade: int) -> Solucao:
    return TabelaDP(itens, capacidade).solucao()


def resolver_por_memoizacao(itens: Sequence[Item], capacidade: int) -> Solucao:
    _validar_capacidade(capacidade)
    itens = tuple(itens)

    @cache
    def melhor(quantidade_de_itens: int, capacidade_restante: int) -> int:
        if quantidade_de_itens == 0 or capacidade_restante == 0:
            return 0
        item = itens[quantidade_de_itens - 1]
        sem_item = melhor(quantidade_de_itens - 1, capacidade_restante)
        if item.peso > capacidade_restante:
            return sem_item
        com_item = item.valor + melhor(quantidade_de_itens - 1, capacidade_restante - item.peso)
        return max(sem_item, com_item)

    indices = _reconstruir(itens, capacidade, melhor)
    return _montar_solucao(itens, indices, melhor.cache_info().currsize)


def resolver_por_forca_bruta(itens: Sequence[Item], capacidade: int) -> Solucao:
    _validar_capacidade(capacidade)
    itens = tuple(itens)
    melhor_indices: tuple[int, ...] = ()
    melhor_valor = 0
    combinacoes_avaliadas = 0
    for tamanho in range(len(itens) + 1):
        for indices in combinations(range(len(itens)), tamanho):
            combinacoes_avaliadas += 1
            if sum(itens[i].peso for i in indices) > capacidade:
                continue
            valor = sum(itens[i].valor for i in indices)
            if valor > melhor_valor:
                melhor_indices, melhor_valor = indices, valor
    return _montar_solucao(itens, melhor_indices, combinacoes_avaliadas)


def _reconstruir(itens: Sequence[Item], capacidade: int, valor: Callable[[int, int], int]) -> tuple[int, ...]:
    escolhidos = []
    capacidade_restante = capacidade
    for quantidade_de_itens in range(len(itens), 0, -1):
        if valor(quantidade_de_itens, capacidade_restante) != valor(quantidade_de_itens - 1, capacidade_restante):
            escolhidos.append(quantidade_de_itens - 1)
            capacidade_restante -= itens[quantidade_de_itens - 1].peso
    return tuple(reversed(escolhidos))


def _montar_solucao(itens: Sequence[Item], indices: Iterable[int], subproblemas: int) -> Solucao:
    indices = tuple(indices)
    return Solucao(
        indices=indices,
        valor=sum(itens[i].valor for i in indices),
        peso=sum(itens[i].peso for i in indices),
        subproblemas=subproblemas,
    )


def _validar_capacidade(capacidade: int) -> None:
    if capacidade < 0:
        raise ValueError("A capacidade não pode ser negativa.")
