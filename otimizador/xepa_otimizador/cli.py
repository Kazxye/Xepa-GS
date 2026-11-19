import argparse
import sys
from collections.abc import Sequence
from datetime import datetime
from decimal import Decimal, InvalidOperation
from pathlib import Path
from time import perf_counter

from xepa_otimizador import relatorio
from xepa_otimizador.leitura import Entrada, EntradaInvalidaError, ler_entrada
from xepa_otimizador.mochila import TabelaDP
from xepa_otimizador.oferta import kg_para_unidades
from xepa_otimizador.planejamento import Metodo, avaliar_disponiveis, planejar_coleta

LIMITE_FORCA_BRUTA = 20


def main(argv: Sequence[str] | None = None) -> int:
    argumentos = _parser().parse_args(argv)
    try:
        entrada = ler_entrada(argumentos.arquivo)
        capacidade_kg = _capacidade(argumentos.capacidade, entrada)
        agora = argumentos.agora or entrada.referencia or datetime.now().astimezone()
        _validar_forca_bruta(argumentos.metodo, entrada)
        plano = planejar_coleta(entrada.ofertas, capacidade_kg, agora, argumentos.metodo)
        secoes = [relatorio.plano(plano, agora)]
        if argumentos.tabela:
            secoes.append(_tabela_dp(entrada, capacidade_kg, agora))
        if argumentos.comparar:
            secoes.append(_comparacao(entrada, capacidade_kg, agora))
    except (EntradaInvalidaError, ValueError) as erro:
        print(f"Erro: {erro}", file=sys.stderr)
        return 1
    print("\n\n".join(secoes))
    return 0


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="xepa_otimizador",
        description="Escolhe quais ofertas uma cozinha deve retirar sem passar da sua capacidade de carga.",
    )
    parser.add_argument("arquivo", type=Path, help="JSON com as ofertas disponíveis")
    parser.add_argument("--capacidade", type=_kg, help="capacidade de carga da cozinha em kg")
    parser.add_argument("--agora", type=_data_hora, help="momento de referência no formato ISO 8601")
    parser.add_argument(
        "--metodo",
        type=Metodo,
        choices=list(Metodo),
        default=Metodo.TABULACAO,
        help="algoritmo usado na escolha (padrão: tabulacao)",
    )
    parser.add_argument("--tabela", action="store_true", help="mostra a tabela da programação dinâmica")
    parser.add_argument("--comparar", action="store_true", help="compara o resultado e o tempo dos métodos")
    return parser


def _kg(texto: str) -> Decimal:
    try:
        return Decimal(texto.replace(",", "."))
    except InvalidOperation as erro:
        raise argparse.ArgumentTypeError(f"quantidade inválida: {texto}") from erro


def _data_hora(texto: str) -> datetime:
    try:
        momento = datetime.fromisoformat(texto)
    except ValueError as erro:
        raise argparse.ArgumentTypeError(f"data inválida: {texto}") from erro
    return momento if momento.tzinfo else momento.astimezone()


def _capacidade(informada: Decimal | None, entrada: Entrada) -> Decimal:
    capacidade_kg = informada if informada is not None else entrada.capacidade_kg
    if capacidade_kg is None:
        raise ValueError("Informe a capacidade com --capacidade ou no campo capacidadeKg do arquivo.")
    return capacidade_kg


def _validar_forca_bruta(metodo: Metodo, entrada: Entrada) -> None:
    if metodo is Metodo.FORCA_BRUTA and len(entrada.ofertas) > LIMITE_FORCA_BRUTA:
        raise ValueError(f"A força bruta aceita no máximo {LIMITE_FORCA_BRUTA} ofertas.")


def _tabela_dp(entrada: Entrada, capacidade_kg: Decimal, agora: datetime) -> str:
    avaliadas = avaliar_disponiveis(entrada.ofertas, agora)
    tabela = TabelaDP([avaliada.como_item() for avaliada in avaliadas], kg_para_unidades(capacidade_kg))
    return relatorio.tabela_dp(tabela, avaliadas)


def _comparacao(entrada: Entrada, capacidade_kg: Decimal, agora: datetime) -> str:
    cabe_forca_bruta = len(entrada.ofertas) <= LIMITE_FORCA_BRUTA
    metodos = [metodo for metodo in Metodo if metodo is not Metodo.FORCA_BRUTA or cabe_forca_bruta]
    ignorados = [] if cabe_forca_bruta else [Metodo.FORCA_BRUTA]
    return relatorio.comparacao([_medir(entrada, capacidade_kg, agora, metodo) for metodo in metodos], ignorados)


def _medir(entrada: Entrada, capacidade_kg: Decimal, agora: datetime, metodo: Metodo) -> relatorio.Medicao:
    inicio = perf_counter()
    plano = planejar_coleta(entrada.ofertas, capacidade_kg, agora, metodo)
    milissegundos = (perf_counter() - inicio) * 1000
    return relatorio.Medicao(metodo, plano.valor_total, plano.subproblemas, milissegundos)
