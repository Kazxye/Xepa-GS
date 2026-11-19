from pathlib import Path

from xepa_otimizador.cli import main

DADOS = Path(__file__).resolve().parent.parent / "dados"


def test_gera_o_plano_do_exemplo(capsys):
    codigo = main([str(DADOS / "ofertas.json")])

    saida = capsys.readouterr().out
    assert codigo == 0
    assert "Carga: 58,50 de 60,00 kg" in saida
    assert "#109" in saida


def test_mostra_tabela_e_comparacao(capsys):
    codigo = main([str(DADOS / "exemplo-pequeno.json"), "--tabela", "--comparar"])

    saida = capsys.readouterr().out
    assert codigo == 0
    assert "Tabela de programação dinâmica" in saida
    assert "força bruta" in saida


def test_capacidade_informada_substitui_a_do_arquivo(capsys):
    main([str(DADOS / "ofertas.json"), "--capacidade", "12,5", "--metodo", "memoizacao"])

    assert "Capacidade 12,50 kg" in capsys.readouterr().out


def test_erro_de_entrada_retorna_codigo_1(capsys, tmp_path):
    codigo = main([str(tmp_path / "nada.json")])

    assert codigo == 1
    assert "Arquivo não encontrado" in capsys.readouterr().err
