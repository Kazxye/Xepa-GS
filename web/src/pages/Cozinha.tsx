import { Phone } from 'lucide-react'
import { useCallback, useState } from 'react'
import type { FormEvent } from 'react'
import { api, mensagemDeErro } from '../api/cliente'
import type { Oferta } from '../api/tipos'
import { useSessao } from '../auth/sessao'
import { Aviso } from '../components/Aviso'
import { PlacaOferta } from '../components/PlacaOferta'
import { distancia, kg, quando, telefoneLegivel, telefoneParaLink } from '../lib/formato'
import { useAtualizacaoPeriodica } from '../lib/useAtualizacaoPeriodica'

const INTERVALO_ATUALIZACAO = 30_000

type Aba = 'proximas' | 'reservas'

export function Cozinha() {
  const { usuario } = useSessao()
  const dados = usuario?.cozinha
  const [aba, setAba] = useState<Aba>('proximas')
  const [proximas, setProximas] = useState<Oferta[] | null>(null)
  const [reservas, setReservas] = useState<Oferta[] | null>(null)
  const [pesos, setPesos] = useState<Record<number, string>>({})
  const [processando, setProcessando] = useState<number | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  const carregar = useCallback(async () => {
    try {
      const [novasProximas, novasReservas] = await Promise.all([api.ofertasProximas(), api.ofertasReservadas()])
      setProximas(novasProximas)
      setReservas(novasReservas)
    } catch (falha) {
      setErro(mensagemDeErro(falha))
    }
  }, [])

  useAtualizacaoPeriodica(carregar, INTERVALO_ATUALIZACAO)

  async function reservar(oferta: Oferta) {
    setProcessando(oferta.id)
    setErro(null)
    setSucesso(null)
    try {
      const reservada = await api.reservar(oferta.id)
      setProximas((atuais) => atuais?.filter((item) => item.id !== oferta.id) ?? null)
      setReservas((atuais) => [reservada, ...(atuais ?? [])])
      setSucesso(
        `Reservado. Busque ${kg(reservada.quantidadeKg)} kg na barraca ${reservada.feirante.nomeBarraca} e confirme a retirada na aba Suas reservas.`,
      )
    } catch (falha) {
      setErro(mensagemDeErro(falha))
      void carregar()
    } finally {
      setProcessando(null)
    }
  }

  async function confirmarRetirada(evento: FormEvent, oferta: Oferta) {
    evento.preventDefault()
    const peso = Number((pesos[oferta.id] ?? String(oferta.quantidadeKg)).replace(',', '.'))
    if (!peso || peso <= 0) {
      setErro('Informe quantos quilos a cozinha levou.')
      return
    }
    setProcessando(oferta.id)
    setErro(null)
    setSucesso(null)
    try {
      const atualizada = await api.confirmarRetirada(oferta.id, peso)
      setReservas((atuais) => atuais?.map((item) => (item.id === atualizada.id ? atualizada : item)) ?? null)
      setSucesso(
        atualizada.status === 'RETIRADA'
          ? `Retirada confirmada: ${kg(peso)} kg somados ao total do projeto.`
          : 'Retirada confirmada. Falta o feirante confirmar a entrega.',
      )
    } catch (falha) {
      setErro(mensagemDeErro(falha))
    } finally {
      setProcessando(null)
    }
  }

  const reservasAbertas = reservas?.filter((oferta) => oferta.status === 'RESERVADA') ?? []
  const reservasEncerradas = reservas?.filter((oferta) => oferta.status !== 'RESERVADA') ?? []
  const pesoAReservar = reservasAbertas.reduce((soma, oferta) => soma + oferta.quantidadeKg, 0)
  const capacidade = dados?.capacidadeCargaKg ?? 0
  const ocupacao = capacidade > 0 ? Math.min(100, (pesoAReservar / capacidade) * 100) : 0

  return (
    <div className="painel-usuario">
      <header className="saudacao">
        <h1>{dados?.nome ?? usuario?.nome}</h1>
        {dados && (
          <p>
            Mostrando sobras a até {distancia(dados.raioBuscaKm)} de {dados.endereco}. A lista atualiza sozinha a
            cada 30 segundos.
          </p>
        )}
      </header>

      {dados && (
        <section className="carga" aria-label="Carga da próxima viagem">
          <div className="carga-linha">
            <span>Para buscar agora</span>
            <strong>
              {kg(pesoAReservar)} de {kg(capacidade)} kg
            </strong>
          </div>
          <div
            className={`carga-barra${pesoAReservar > capacidade ? ' carga-excedida' : ''}`}
            role="progressbar"
            aria-valuemin={0}
            aria-valuemax={capacidade}
            aria-valuenow={pesoAReservar}
            aria-label="Peso reservado em relação à capacidade de carga"
          >
            <span style={{ width: `${ocupacao}%` }} />
          </div>
          {pesoAReservar > capacidade && (
            <p className="carga-alerta">O peso reservado passa do que a equipe carrega. Planeje mais de uma viagem.</p>
          )}
        </section>
      )}

      <div className="abas" role="tablist" aria-label="Ofertas">
        <button type="button" role="tab" aria-selected={aba === 'proximas'} onClick={() => setAba('proximas')}>
          Perto da cozinha {proximas && <span className="abas-contagem">{proximas.length}</span>}
        </button>
        <button type="button" role="tab" aria-selected={aba === 'reservas'} onClick={() => setAba('reservas')}>
          Suas reservas {reservasAbertas.length > 0 && <span className="abas-contagem">{reservasAbertas.length}</span>}
        </button>
      </div>

      <Aviso tipo="erro" mensagem={erro} />
      <Aviso tipo="sucesso" mensagem={sucesso} />

      {aba === 'proximas' && (
        <section role="tabpanel" aria-label="Perto da cozinha">
          {proximas === null && !erro && <p className="carregando">Procurando sobras perto da cozinha...</p>}
          {proximas !== null && proximas.length === 0 && (
            <p className="vazio">
              Nenhuma sobra disponível perto da cozinha agora. As feiras costumam publicar entre 11h e 13h.
            </p>
          )}
          <div className="lista-placas">
            {proximas?.map((oferta) => (
              <PlacaOferta key={oferta.id} oferta={oferta}>
                <button
                  type="button"
                  className="botao botao-primario"
                  disabled={processando === oferta.id}
                  onClick={() => void reservar(oferta)}
                >
                  {processando === oferta.id ? 'Reservando...' : 'Reservar'}
                </button>
              </PlacaOferta>
            ))}
          </div>
        </section>
      )}

      {aba === 'reservas' && (
        <section role="tabpanel" aria-label="Suas reservas">
          {reservas !== null && reservasAbertas.length === 0 && (
            <p className="vazio">Nenhuma retirada pendente. Reserve uma oferta na aba Perto da cozinha.</p>
          )}
          <div className="lista-placas">
            {reservasAbertas.map((oferta) => {
              const retirada = oferta.reserva?.retirada
              return (
                <PlacaOferta key={oferta.id} oferta={oferta}>
                  <a className="botao botao-secundario" href={telefoneParaLink(oferta.feirante.telefone)}>
                    <Phone size={18} aria-hidden="true" />
                    Ligar para {oferta.feirante.nome.split(' ')[0]} {telefoneLegivel(oferta.feirante.telefone)}
                  </a>
                  {retirada?.confirmadoCozinhaEm ? (
                    <p className="placa-mensagem">
                      Você informou {kg(retirada.pesoKg ?? 0)} kg. Falta o feirante confirmar a entrega.
                    </p>
                  ) : (
                    <form className="confirmar-peso" onSubmit={(evento) => void confirmarRetirada(evento, oferta)}>
                      <label htmlFor={`peso-${oferta.id}`}>Quantos kg vocês levaram?</label>
                      <div className="confirmar-peso-linha">
                        <input
                          id={`peso-${oferta.id}`}
                          inputMode="decimal"
                          value={pesos[oferta.id] ?? String(oferta.quantidadeKg).replace('.', ',')}
                          onChange={(evento) => setPesos({ ...pesos, [oferta.id]: evento.target.value })}
                        />
                        <button type="submit" className="botao botao-primario" disabled={processando === oferta.id}>
                          {processando === oferta.id ? 'Confirmando...' : 'Confirmar retirada'}
                        </button>
                      </div>
                    </form>
                  )}
                </PlacaOferta>
              )
            })}
          </div>

          {reservasEncerradas.length > 0 && (
            <>
              <h2 className="titulo-historico">Histórico</h2>
              <ul className="historico">
                {reservasEncerradas.map((oferta) => (
                  <li key={oferta.id}>
                    <div>
                      <p className="historico-titulo">{oferta.descricao}</p>
                      <p className="historico-detalhe">
                        {oferta.status === 'RETIRADA'
                          ? `${kg(oferta.reserva?.retirada?.pesoKg ?? oferta.quantidadeKg)} kg da ${oferta.feirante.nomeBarraca}`
                          : `Reserva expirou sem retirada, ${oferta.feirante.nomeBarraca}`}
                        , {quando(oferta.reserva?.reservadaEm ?? oferta.criadaEm)}
                      </p>
                    </div>
                  </li>
                ))}
              </ul>
            </>
          )}
        </section>
      )}
    </div>
  )
}
