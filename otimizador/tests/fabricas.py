from datetime import datetime, timedelta, timezone
from decimal import Decimal

from xepa_otimizador.oferta import Oferta, TipoAlimento

BRASILIA = timezone(timedelta(hours=-3))
AGORA = datetime(2026, 11, 16, 12, 30, tzinfo=BRASILIA)


def criar_oferta(
    id: int = 1,
    tipo: TipoAlimento = TipoAlimento.FRUTA,
    quantidade_kg: str = "10",
    prazo: timedelta = timedelta(hours=2),
) -> Oferta:
    return Oferta(
        id=id,
        tipo=tipo,
        descricao=f"Oferta {id}",
        quantidade_kg=Decimal(quantidade_kg),
        horario_limite=AGORA + prazo,
        feira="Feira da Rua Áurea",
    )

