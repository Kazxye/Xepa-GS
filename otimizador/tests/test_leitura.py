import json
from decimal import Decimal

import pytest

from xepa_otimizador.leitura import EntradaInvalidaError, entrada_de_json, ler_entrada
from xepa_otimizador.oferta import TipoAlimento

OFERTA_DA_API = {
    "id": 7,
    "tipo": "VERDURA",
    "descricao": "Couve",
    "quantidadeKg": 12.5,
    "horarioLimite": "2026-11-16T16:00:00Z",
    "status": "DISPONIVEL",
    "feira": {"id": 1, "nome": "Feira da Rua Áurea"},
}


def test_le_arquivo_com_cabecalho(tmp_path):
    arquivo = tmp_path / "ofertas.json"
    conteudo = {"referencia": "2026-11-16T12:30:00-03:00", "capacidadeKg": 60, "ofertas": [OFERTA_DA_API]}
    arquivo.write_text(json.dumps(conteudo), encoding="utf-8")

    entrada = ler_entrada(arquivo)

    assert entrada.capacidade_kg == Decimal("60")
    assert entrada.referencia.isoformat() == "2026-11-16T12:30:00-03:00"
    oferta = entrada.ofertas[0]
    assert oferta.tipo is TipoAlimento.VERDURA
    assert oferta.quantidade_kg == Decimal("12.5")
    assert oferta.feira == "Feira da Rua Áurea"


def test_aceita_a_lista_devolvida_pela_api():
    entrada = entrada_de_json([OFERTA_DA_API])

    assert len(entrada.ofertas) == 1
    assert entrada.capacidade_kg is None
    assert entrada.referencia is None


@pytest.mark.parametrize(
    ("alteracao", "mensagem"),
    [
        ({"tipo": "CARNE"}, "Oferta 1"),
        ({"quantidadeKg": "muito"}, "Oferta 1"),
        ({"horarioLimite": "amanhã"}, "Oferta 1"),
    ],
)
def test_oferta_invalida_informa_a_posicao(alteracao, mensagem):
    with pytest.raises(EntradaInvalidaError, match=mensagem):
        entrada_de_json([OFERTA_DA_API | alteracao])


def test_campo_ausente_e_informado():
    incompleta = {chave: valor for chave, valor in OFERTA_DA_API.items() if chave != "descricao"}

    with pytest.raises(EntradaInvalidaError, match="descricao"):
        entrada_de_json([incompleta])


def test_json_malformado_e_rejeitado(tmp_path):
    arquivo = tmp_path / "quebrado.json"
    arquivo.write_text("{", encoding="utf-8")

    with pytest.raises(EntradaInvalidaError, match="JSON inválido"):
        ler_entrada(arquivo)


def test_arquivo_inexistente_e_rejeitado(tmp_path):
    with pytest.raises(EntradaInvalidaError, match="não encontrado"):
        ler_entrada(tmp_path / "nada.json")


def test_conteudo_que_nao_e_lista_nem_objeto_e_rejeitado():
    with pytest.raises(EntradaInvalidaError):
        entrada_de_json("ofertas")
