# Data Science

Entrega de Data Science and Statistical Computing.

O documento está em [`mestre da estatística blaster master.pdf`](mestre%20da%20estat%C3%ADstica%20blaster%20master.pdf). Ele ocupa uma página A4, como o enunciado pede.

## O custo invisível do café

A disciplina teve um desafio próprio dentro do tema CitizenTech. A tarefa foi analisar amostras de cápsulas de oito marcas fictícias de café e propor a melhor estratégia de consumo consciente, levando em conta os impactos ambientais e sociais.

| Item | Conteúdo |
|---|---|
| Dados | 8 marcas, 640 cápsulas cada (5.120 registros sintéticos) |
| Critério de "melhor" | Primeiro o risco de dano a pessoas, depois a emissão de CO₂e por cápsula |
| Recomendação | Aurora, com devolução da cápsula para coleta |
| Métodos | Teste z de duas proporções com correção de Holm, teste t de Welch, intervalos de Wilson e Newcombe, bootstrap e Mann-Whitney |

## Principais evidências

- **Filtro social:** só a Aurora não difere da Delta, a marca com menor taxa de trabalho infantil entre os fornecedores auditados (+2,4 p.p., IC 95% de −0,9 a +5,7). Todas as outras ficam acima dela.
- **Clima:** a Aurora emite 35,9 g de CO₂e a menos que a Delta por cápsula (IC 95% de −38,6 a −33,2).
- **Descarte:** sem coleta não há reciclagem. Metade das cápsulas coletadas foi reciclada e nenhuma das não coletadas.
- **Alegações das marcas:** "reciclável", "compensado" e "compostável" não se sustentam nos dados.

O documento também traz os testes de robustez, a análise de sensibilidade e as limitações.

## Responsável

Gustavo Estevam Cocchi, com revisão da squad.
