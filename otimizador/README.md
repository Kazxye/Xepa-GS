# Otimizador de coleta

Entrega de Dynamic Programming da Xepa Solidária.

## O problema

No fim da feira, uma cozinha comunitária vê várias ofertas de sobras por perto, mas o carro ou o carrinho dela só leva uma certa quantidade de quilos (`capacidadeCargaKg` no cadastro da cozinha). Cada oferta é reservada inteira, então não dá para levar meia caixa. A pergunta é: **quais ofertas a cozinha deve reservar para aproveitar melhor a carga?**

Esse é o problema da mochila 0/1:

| Mochila | Xepa Solidária |
|---|---|
| Capacidade | Capacidade de carga da cozinha |
| Item | Oferta disponível |
| Peso | Quilos da oferta |
| Valor | Quilos ponderados pela perecibilidade e pela urgência |
| Pegar ou não pegar | Reservar a oferta inteira ou deixar para outra cozinha |

## Valor de uma oferta

```
valor = peso em unidades × perecibilidade × urgência
```

- **Unidades:** 1 unidade = 0,01 kg (10 g). O peso vira número inteiro, que é o que a tabela precisa. Uma capacidade de 60 kg tem 6.000 unidades.
- **Perecibilidade:**

  | Tipo | Perecibilidade |
  |---|---|
  | Verdura | 3 |
  | Fruta | 2 |
  | Legume | 1 |

- **Urgência:**

  | Tempo até o horário limite | Urgência |
  |---|---|
  | até 1 h | 4 |
  | até 3 h | 3 |
  | até 6 h | 2 |
  | mais de 6 h | 1 |

As ofertas com prazo vencido saem da conta antes do cálculo.

## Recorrência

Seja `V(i, c)` o maior valor possível usando só as `i` primeiras ofertas com capacidade `c`. Para a oferta `i`, de peso `p` e valor `v`:

```
V(0, c) = 0
V(i, 0) = 0
V(i, c) = V(i - 1, c)                                  se p > c
V(i, c) = max(V(i - 1, c), v + V(i - 1, c - p))        caso contrário
```

O primeiro termo do `max` é deixar a oferta; o segundo é levá-la e resolver o resto com a capacidade que sobrou. A resposta é `V(n, C)`.

Para saber **quais** ofertas foram escolhidas, o algoritmo volta da última linha para a primeira. Se `V(i, c)` é diferente de `V(i - 1, c)`, a oferta `i` entrou na solução e a capacidade diminui do peso dela.

## Três implementações

O arquivo [`xepa_otimizador/mochila.py`](xepa_otimizador/mochila.py) resolve o mesmo problema de três formas.

| Método | Como funciona | Tempo | Memória |
|---|---|---|---|
| Tabulação (bottom-up) | `TabelaDP` preenche a tabela `(n + 1) × (C + 1)` linha por linha, do caso base até a resposta | O(n × C) | O(n × C) |
| Memoização (top-down) | Função recursiva com `functools.cache`; só calcula os subproblemas que a resposta realmente usa | O(n × C) no pior caso | O(n × C) no pior caso |
| Força bruta | Testa todas as combinações de ofertas; serve de referência nos testes | O(2ⁿ × n) | O(n) |

`n` é o número de ofertas e `C` a capacidade em unidades.

Com 10 ofertas e 60 kg:

- a tabulação resolve 60.010 subproblemas;
- a memoização resolve 113;
- a força bruta testa 512 combinações das 9 ofertas ainda no prazo.

Com 30 ofertas, a força bruta já passaria de 1 bilhão de combinações. A programação dinâmica continua proporcional a `n × C`.

### Por que não escolher na base do "mais valioso por quilo"?

A escolha gulosa pega primeiro a maior razão valor/peso e pode errar. Com capacidade 50 e as ofertas abaixo, ela leva A e B (valor 160). A resposta certa é B e C (valor 220):

| Oferta | Peso | Valor | Valor por peso |
|---|---|---|---|
| A | 10 | 60 | 6,0 |
| B | 20 | 100 | 5,0 |
| C | 30 | 120 | 4,0 |

Esse caso está no teste `test_programacao_dinamica_supera_a_escolha_gulosa`.

## Estrutura

| Arquivo | Responsabilidade |
|---|---|
| `mochila.py` | Algoritmos genéricos da mochila 0/1 (`Item`, `Solucao`, `TabelaDP`) |
| `oferta.py` | `Oferta`, `TipoAlimento`, conversão de kg em unidades e cálculo do valor |
| `planejamento.py` | Liga as ofertas aos algoritmos e monta o `PlanoDeColeta` |
| `leitura.py` | Lê o JSON de entrada e valida cada oferta |
| `relatorio.py` | Formata o plano, a tabela e a comparação para o terminal |
| `cli.py` | Argumentos da linha de comando |

Os algoritmos não conhecem ofertas nem quilos, só pesos e valores inteiros.

## Como rodar

Requer Python 3.12 ou superior. Na pasta `otimizador`:

```bash
python -m xepa_otimizador dados/ofertas.json
```

| Opção | Efeito |
|---|---|
| `--capacidade 45` | Usa outra capacidade em kg (aceita vírgula) |
| `--agora 2026-11-16T13:00:00-03:00` | Muda o momento de referência das urgências |
| `--metodo memoizacao` | Escolhe `tabulacao` (padrão), `memoizacao` ou `forca-bruta` |
| `--tabela` | Mostra a tabela da programação dinâmica |
| `--comparar` | Compara valor, subproblemas e tempo dos três métodos |

Para ver a tabela inteira num caso pequeno:

```bash
python -m xepa_otimizador dados/exemplo-pequeno.json --tabela --comparar
```

## Formato da entrada

```json
{
  "referencia": "2026-11-16T12:30:00-03:00",
  "capacidadeKg": 60,
  "ofertas": [
    {
      "id": 101,
      "tipo": "VERDURA",
      "descricao": "Alface crespa e couve",
      "quantidadeKg": 12.0,
      "horarioLimite": "2026-11-16T13:00:00-03:00",
      "feira": { "id": 1, "nome": "Feira da Rua Joaquim Távora" }
    }
  ]
}
```

- `referencia` e `capacidadeKg` são opcionais. Sem `referencia`, vale o horário atual.
- As ofertas usam os mesmos campos da API. Por isso, a lista devolvida por `GET /api/ofertas/proximas` também é aceita direto, com a capacidade em `--capacidade`.

## Testes

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
pytest
```

Os testes cobrem:

- o exemplo clássico da mochila e o caso em que a escolha gulosa falha;
- casos de borda: capacidade zero, lista vazia e oferta maior que a carga;
- 300 instâncias aleatórias em que tabulação, memoização e força bruta precisam concordar;
- regras de urgência e perecibilidade;
- leitura e validação do JSON;
- a linha de comando.
