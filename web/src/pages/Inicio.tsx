import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import type { Painel } from '../api/tipos'
import { rotaInicial, useSessao } from '../auth/sessao'
import { Aviso } from '../components/Aviso'
import { inteiro, kg } from '../lib/formato'

const PASSOS = [
  {
    titulo: 'O feirante avisa a sobra',
    texto: 'No fim da feira, informa o que sobrou, quantos quilos e até que horas dá para buscar.',
  },
  {
    titulo: 'As cozinhas perto recebem',
    texto: 'Só aparecem as ofertas dentro da distância que a cozinha consegue percorrer a pé ou de carrinho.',
  },
  {
    titulo: 'Uma cozinha reserva',
    texto: 'A primeira que reservar fica com a oferta. As outras veem que ela já tem destino.',
  },
  {
    titulo: 'Os dois confirmam a retirada',
    texto: 'A cozinha informa o peso que levou, o feirante confirma a entrega e o total abaixo é atualizado.',
  },
]

export function Inicio() {
  const { usuario } = useSessao()
  const [painel, setPainel] = useState<Painel | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    let ativo = true
    api
      .painel()
      .then((dados) => ativo && setPainel(dados))
      .catch((falha) => ativo && setErro(mensagemDeErro(falha)))
    return () => {
      ativo = false
    }
  }, [])

  const maiorFeira = Math.max(1, ...(painel?.porFeira.map((feira) => feira.quilosRecuperados) ?? [1]))

  return (
    <>
      <section className="abertura">
        <div className="abertura-texto">
          <h1>A sobra da feira vai para a cozinha do bairro no mesmo dia.</h1>
          <p className="abertura-resumo">
            No fim da feira livre sobra fruta, legume e verdura bons para comer. A poucas quadras, cozinhas
            comunitárias cozinham para quem passa fome. A Xepa Solidária avisa a cozinha a tempo de buscar.
          </p>
          {usuario ? (
            <div className="abertura-acoes">
              <Link className="botao botao-primario" to={rotaInicial(usuario)}>
                {usuario.perfil === 'FEIRANTE' ? 'Ir para minha barraca' : 'Ver ofertas perto da cozinha'}
              </Link>
            </div>
          ) : (
            <div className="abertura-acoes">
              <Link className="botao botao-primario" to="/cadastro?perfil=feirante">
                Quero doar minha sobra
              </Link>
              <Link className="botao botao-secundario" to="/cadastro?perfil=cozinha">
                Cadastrar uma cozinha
              </Link>
            </div>
          )}
        </div>

        <figure className="placa-total" aria-live="polite">
          <p className="placa-total-numero">
            {painel ? kg(painel.quilosRecuperados) : '...'}
            <span>kg</span>
          </p>
          <figcaption>de alimento que deixou de ir para o lixo nas feiras do piloto</figcaption>
        </figure>
      </section>

      <Aviso tipo="erro" mensagem={erro} />

      {painel && (
        <section className="numeros" aria-label="Resultados do piloto">
          <dl>
            <div>
              <dt>Retiradas concluídas</dt>
              <dd>{inteiro(painel.retiradasConcluidas)}</dd>
            </div>
            <div>
              <dt>Cozinhas atendidas</dt>
              <dd>{inteiro(painel.cozinhasAtendidas)}</dd>
            </div>
            <div>
              <dt>Feirantes que doaram</dt>
              <dd>{inteiro(painel.feirantesDoadores)}</dd>
            </div>
            <div>
              <dt>Ofertas que chegaram a uma cozinha</dt>
              <dd>{kg(painel.taxaDeRetiradaPercentual)}%</dd>
            </div>
            {painel.tempoMedioAteReservaMinutos !== undefined && (
              <div>
                <dt>Tempo médio até a reserva</dt>
                <dd>{inteiro(Math.round(painel.tempoMedioAteReservaMinutos))} min</dd>
              </div>
            )}
          </dl>
        </section>
      )}

      <section className="secao">
        <h2>Como funciona</h2>
        <ol className="passos">
          {PASSOS.map((passo) => (
            <li key={passo.titulo}>
              <h3>{passo.titulo}</h3>
              <p>{passo.texto}</p>
            </li>
          ))}
        </ol>
      </section>

      {painel && painel.porFeira.length > 0 && (
        <section className="secao">
          <h2>Quilos recuperados por feira</h2>
          <ul className="ranking">
            {painel.porFeira.map((feira) => (
              <li key={feira.feiraId}>
                <div className="ranking-linha">
                  <span className="ranking-nome">{feira.feiraNome}</span>
                  <span className="ranking-valor">{kg(feira.quilosRecuperados)} kg</span>
                </div>
                <div className="ranking-barra" aria-hidden="true">
                  <span style={{ width: `${(feira.quilosRecuperados / maiorFeira) * 100}%` }} />
                </div>
                <p className="ranking-detalhe">
                  {feira.retiradasConcluidas} de {feira.ofertasPublicadas}{' '}
                  {feira.ofertasPublicadas === 1 ? 'oferta retirada' : 'ofertas retiradas'}
                </p>
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  )
}
