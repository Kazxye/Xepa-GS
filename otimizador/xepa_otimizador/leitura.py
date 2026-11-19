import json
from collections.abc import Mapping
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any

from xepa_otimizador.oferta import Oferta, TipoAlimento


class EntradaInvalidaError(Exception):
    pass


@dataclass(frozen=True, slots=True)
class Entrada:
    ofertas: tuple[Oferta, ...]
    capacidade_kg: Decimal | None = None
    referencia: datetime | None = None


def ler_entrada(caminho: Path) -> Entrada:
    try:
        conteudo = json.loads(caminho.read_text(encoding="utf-8"), parse_float=Decimal)
    except FileNotFoundError as erro:
        raise EntradaInvalidaError(f"Arquivo não encontrado: {caminho}.") from erro
    except OSError as erro:
        raise EntradaInvalidaError(f"Não foi possível ler o arquivo {caminho}.") from erro
    except json.JSONDecodeError as erro:
        raise EntradaInvalidaError(f"JSON inválido em {caminho}, linha {erro.lineno}.") from erro
    return entrada_de_json(conteudo)


def entrada_de_json(conteudo: Any) -> Entrada:
    if isinstance(conteudo, list):
        return Entrada(ofertas=_ofertas_de_json(conteudo))
    if not isinstance(conteudo, Mapping):
        raise EntradaInvalidaError("O arquivo deve conter uma lista de ofertas ou um objeto com o campo ofertas.")
    try:
        return Entrada(
            ofertas=_ofertas_de_json(conteudo.get("ofertas", [])),
            capacidade_kg=_decimal_opcional(conteudo.get("capacidadeKg")),
            referencia=_data_opcional(conteudo.get("referencia")),
        )
    except (ValueError, TypeError, InvalidOperation) as erro:
        raise EntradaInvalidaError(f"Cabeçalho inválido: {erro}") from erro


def oferta_de_json(dado: Mapping[str, Any]) -> Oferta:
    feira = dado.get("feira") or {}
    return Oferta(
        id=int(dado["id"]),
        tipo=TipoAlimento(dado["tipo"]),
        descricao=str(dado["descricao"]),
        quantidade_kg=Decimal(str(dado["quantidadeKg"])),
        horario_limite=datetime.fromisoformat(dado["horarioLimite"]),
        feira=str(feira.get("nome", "")),
    )


def _ofertas_de_json(dados: Any) -> tuple[Oferta, ...]:
    if not isinstance(dados, list):
        raise EntradaInvalidaError("O campo ofertas deve ser uma lista.")
    ofertas = []
    for posicao, dado in enumerate(dados, start=1):
        try:
            ofertas.append(oferta_de_json(dado))
        except KeyError as erro:
            raise EntradaInvalidaError(f"Oferta {posicao}: falta o campo {erro.args[0]}.") from erro
        except (ValueError, TypeError, AttributeError, InvalidOperation) as erro:
            raise EntradaInvalidaError(f"Oferta {posicao}: {erro}") from erro
    return tuple(ofertas)


def _decimal_opcional(valor: Any) -> Decimal | None:
    return None if valor is None else Decimal(str(valor))


def _data_opcional(valor: Any) -> datetime | None:
    return None if valor is None else datetime.fromisoformat(valor)
