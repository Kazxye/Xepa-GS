# Xepa Solidária

Sobras de feira livre para cozinhas comunitárias do mesmo bairro. Global Solution 2026.2, tema CitizenTech, eixo Alimentação.

No fim de cada feira sobra comida boa que o feirante não consegue vender nem levar de volta. A poucas quadras, cozinhas comunitárias dependem de doações que chegam sem previsão. Na Xepa Solidária o feirante avisa o que sobrou e até que horas pode ser retirado, uma cozinha próxima reserva e vai buscar, e cada retirada confirmada soma no total de quilos que deixaram de ir para o lixo.

A visão geral do projeto, com o problema, a solução e como ela foi construída, está em [`visao-geral.pdf`](visao-geral.pdf).

## Entregas por disciplina

| Disciplina | Pasta |
|---|---|
| Domain Driven Design | [`api/`](api) |
| Database Design | [`database/`](database) |
| Dynamic Programming | [`otimizador/`](otimizador) |
| Network Architect Solutions | [`network/`](network) |
| Data Science and Statistical Computing | [`data-science/`](data-science) |
| Agile Methodology with Squad Framework | [`agile/`](agile) |

O aplicativo web fica em [`web/`](web) e é a base comum de todas as entregas.

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `api/` | API em Java 21 com Spring Boot, organizada em DDD |
| `web/` | Site em React com TypeScript |
| `database/` | Modelo do banco, dicionário de dados e scripts SQL |
| `otimizador/` | Programação dinâmica em Python para planejar a coleta |
| `network/` | Topologia de rede no Cisco Packet Tracer |
| `data-science/` | Análise estatística do desafio O custo invisível do café |
| `agile/` | Organização da squad, backlog e sprints |

## Como rodar localmente

API (requer JDK 21):

```bash
cd api
./mvnw spring-boot:run
```

Site (requer Node.js 20 ou superior):

```bash
cd web
npm install
npm run dev
```

Otimizador (requer Python 3.12 ou superior):

```bash
cd otimizador
python -m xepa_otimizador dados/ofertas.json
```

## Equipe

| Nome | RM |
|---|---|
| Arthur Pastorello | 562345 |
| Gustavo Estevam Cocchi | 562472 |
| José Henrique Escobar | 564419 |
| Juan Pablo Molina Molina | 564445 |
| Kazys | 564020 |
