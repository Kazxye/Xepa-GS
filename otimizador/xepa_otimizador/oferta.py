from dataclasses import dataclass
from datetime import datetime, timedelta
from decimal import Decimal
from enum import StrEnum

UNIDADES_POR_KG = 100
QUANTIDADE_MAXIMA_KG = Decimal("1000")

FAIXAS_DE_URGENCIA = (
    (timedelta(hours=1), 4),
    (timedelta(hours=3), 3),
    (timedelta(hours=6), 2),
)
URGENCIA_MINIMA = 1


class TipoAlimento(StrEnum):
    FRUTA = "FRUTA"
    LEGUME = "LEGUME"
    VERDURA = "VERDURA"

    @property
    def nome(self) -> str:
        return self.value.capitalize()

    @property
    def perecibilidade(self) -> int:
        return PERECIBILIDADE[self]


PERECIBILIDADE = {
    TipoAlimento.LEGUME: 1,
    TipoAlimento.FRUTA: 2,
    TipoAlimento.VERDURA: 3,
}


def kg_para_unidades(quantidade_kg: Decimal) -> int:
    unidades = quantidade_kg * UNIDADES_POR_KG
    if unidades != unidades.to_integral_value():
        raise ValueError(f"Use no máximo duas casas decimais em {quantidade_kg} kg.")
    return int(unidades)


@dataclass(frozen=True, slots=True)
class Oferta:
    id: int
    tipo: TipoAlimento
    descricao: str
    quantidade_kg: Decimal
    horario_limite: datetime
    feira: str

    def __post_init__(self) -> None:
        if not Decimal("0") < self.quantidade_kg <= QUANTIDADE_MAXIMA_KG:
            raise ValueError(f"A quantidade deve estar entre 0 e {QUANTIDADE_MAXIMA_KG} kg.")
        kg_para_unidades(self.quantidade_kg)
        if self.horario_limite.tzinfo is None:
            raise ValueError("O horário limite precisa informar o fuso horário.")

    @property
    def peso_em_unidades(self) -> int:
        return kg_para_unidades(self.quantidade_kg)

    def tempo_restante(self, agora: datetime) -> timedelta:
        return self.horario_limite - agora

    def disponivel_em(self, agora: datetime) -> bool:
        return self.tempo_restante(agora) > timedelta(0)

    def urgencia(self, agora: datetime) -> int:
        restante = self.tempo_restante(agora)
        for limite, urgencia in FAIXAS_DE_URGENCIA:
            if restante <= limite:
                return urgencia
        return URGENCIA_MINIMA

    def valor(self, agora: datetime) -> int:
        return self.peso_em_unidades * self.tipo.perecibilidade * self.urgencia(agora)
