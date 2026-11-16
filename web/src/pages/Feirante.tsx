import { Apple, Carrot, Leaf, Minus, Phone, Plus, RotateCcw, Share2 } from 'lucide-react'
import { useCallback, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, api, mensagemDeErro } from '../api/cliente'
import type { Oferta, TipoAlimento } from '../api/tipos'
import { useSessao } from '../auth/sessao'
import { Aviso } from '../components/Aviso'
import { Campo } from '../components/Campo'
import { Escolha } from '../components/Escolha'
import { PlacaOferta } from '../components/PlacaOferta'
import { NOME_DIA, frase, horario, kg, quando, telefoneLegivel, telefoneParaLink } from '../lib/formato'
import { useAtualizacaoPeriodica } from '../lib/useAtualizacaoPeriodica'

const INTERVALO_ATUALIZACAO = 30_000
const PRAZOS = [
  { valor: 60, rotulo: 'Em 1 hora' },
  { valor: 120, rotulo: 'Em 2 horas' },
  { valor: 180, rotulo: 'Em 3 horas' },
]
const QUANTIDADES_RAPIDAS = [5, 10, 20, 30]

interface Formulario {
  tipo: TipoAlimento
  descricao: string
  quantidade: string
  prazoMinutos: number | null
  horarioPersonalizado: string
}

const FORMULARIO_VAZIO: Formulario = {
  tipo: 'FRUTA',
  descricao: '',
  quantidade: '10',
  prazoMinutos: 120,
  horarioPersonalizado: '',
}

function calcularHorarioLimite(formulario: Formulario): Date | null {
  if (formulario.prazoMinutos !== null) {
    return new Date(Date.now() + formulario.prazoMinutos * 60_000)
  }
  if (!formulario.horarioPersonalizado) {
    return null
  }
  const [horas, minutos] = formulario.horarioPersonalizado.split(':').map(Number)
  const limite = new Date()
  limite.setHours(horas, minutos, 0, 0)
  return limite
}

function mensagemCompartilhamento(oferta: Oferta): string {
  return [
    `Sobra disponível na Xepa Solidária: ${kg(oferta.quantidadeKg)} kg de ${oferta.descricao.toLowerCase()}.`,
    `${oferta.feirante.nomeBarraca}, ${oferta.feira.nome}.`,
    `Retirar até ${horario(oferta.horarioLimite)}. Reserve pelo aplicativo: ${window.location.origin}/cozinha`,
  ].join(' ')
}

export function Feirante() {
  const { usuario } = useSessao()
  const dados = usuario?.feirante
  const [formulario, setFormulario] = useState<Formulario>(FORMULARIO_VAZIO)
  const [ofertas, setOfertas] = useState<Oferta[] | null>(null)
  const [publicando, setPublicando] = useState(false)
  const [confirmando, setConfirmando] = useState<number | null>(null)
  const [erroFormulario, setErroFormulario] = useState<string | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [sucesso, setSucesso] = useState<string | null>(null)
  const [erroLista, setErroLista] = useState<string | null>(null)
  const campoDescricao = useRef<HTMLDivElement>(null)

  const carregar = useCallback(async () => {
    try {
      setOfertas(await api.minhasOfertas())
      setErroLista(null)
    } catch (falha) {
      setErroLista(mensagemDeErro(falha))
    }
  }, [])

  useAtualizacaoPeriodica(carregar, INTERVALO_ATUALIZACAO)

  function alterarQuantidade(delta: number) {
    const atual = Number(formulario.quantidade.replace(',', '.')) || 0
    const nova = Math.min(1000, Math.max(1, Math.round(atual + delta)))
    setFormulario({ ...formulario, quantidade: String(nova) })
  }

  function repetir(oferta: Oferta) {
    setFormulario({
      ...FORMULARIO_VAZIO,
      tipo: oferta.tipo,
      descricao: oferta.descricao,
      quantidade: String(oferta.quantidadeKg).replace('.', ','),
    })
    setSucesso(null)
    campoDescricao.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    campoDescricao.current?.querySelector('input')?.focus({ preventScroll: true })
  }

  async function publicar(evento: FormEvent) {
    evento.preventDefault()
    setErroFormulario(null)
    setCampos({})
    setSucesso(null)

    const limite = calcularHorarioLimite(formulario)
    if (!limite) {
      setErroFormulario('Escolha até que horas a sobra pode ser retirada.')
      return
    }
    if (limite.getTime() <= Date.now()) {
      setErroFormulario('O horário de retirada precisa ser depois de agora.')
      return
    }

    setPublicando(true)
    try {
      const publicada = await api.publicarOferta({
        tipo: formulario.tipo,
        descricao: formulario.descricao,
        quantidadeKg: Number(formulario.quantidade.replace(',', '.')),
        horarioLimite: limite.toISOString(),
      })
      setOfertas((anteriores) => [publicada, ...(anteriores ?? [])])
      setFormulario(FORMULARIO_VAZIO)
      setSucesso('Oferta publicada. As cozinhas próximas já podem ver e reservar.')
    } catch (falha) {
      setErroFormulario(mensagemDeErro(falha))
      if (falha instanceof ApiError) {
        setCampos(falha.campos)
      }
    } finally {
      setPublicando(false)
    }
  }

  async function confirmarEntrega(oferta: Oferta) {
    setConfirmando(oferta.id)
    setErroLista(null)
    try {
      const atualizada = await api.confirmarEntrega(oferta.id)
      setOfertas((anteriores) => anteriores?.map((item) => (item.id === atualizada.id ? atualizada : item)) ?? null)
    } catch (falha) {
      setErroLista(mensagemDeErro(falha))
    } finally {
      setConfirmando(null)
    }
  }

  const ativas = ofertas?.filter((oferta) => oferta.status === 'DISPONIVEL' || oferta.status === 'RESERVADA') ?? []
  const encerradas = ofertas?.filter((oferta) => oferta.status === 'RETIRADA' || oferta.status === 'EXPIRADA') ?? []
  const totalDoado = encerradas
    .filter((oferta) => oferta.status === 'RETIRADA')
    .reduce((soma, oferta) => soma + (oferta.reserva?.retirada?.pesoKg ?? 0), 0)

  return (
    <div className="painel-usuario">
      <header className="saudacao">
        <h1>{dados?.nomeBarraca ?? usuario?.nome}</h1>
        {dados && (
          <p>
            {dados.feira.nome}, toda {NOME_DIA[dados.feira.diaSemana]}. Você já doou {kg(totalDoado)} kg pelo
            aplicativo.
          </p>
        )}
      </header>

      <div className="duas-colunas">
        <section className="cartao-formulario" aria-labelledby="titulo-publicar">
          <h2 id="titulo-publicar">Avisar sobra</h2>
          <form className="formulario" onSubmit={publicar} noValidate>
            <Escolha<TipoAlimento>
              legenda="O que sobrou"
              valor={formulario.tipo}
              aoMudar={(tipo) => setFormulario({ ...formulario, tipo })}
              opcoes={[
                { valor: 'FRUTA', rotulo: 'Fruta', icone: <Apple size={20} aria-hidden="true" /> },
                { valor: 'LEGUME', rotulo: 'Legume', icone: <Carrot size={20} aria-hidden="true" /> },
                { valor: 'VERDURA', rotulo: 'Verdura', icone: <Leaf size={20} aria-hidden="true" /> },
              ]}
            />

            <div ref={campoDescricao}>
              <Campo
                rotulo="Descrição"
                value={formulario.descricao}
                onChange={(evento) => setFormulario({ ...formulario, descricao: evento.target.value })}
                placeholder="Ex.: tomate italiano maduro, 2 caixas"
                maxLength={255}
                erro={campos.descricao}
                required
              />
            </div>

            <div className={`campo${campos.quantidadeKg ? ' campo-com-erro' : ''}`}>
              <label htmlFor="quantidade">Quantidade aproximada em kg</label>
              <div className="contador">
                <button type="button" onClick={() => alterarQuantidade(-1)} aria-label="Diminuir 1 kg">
                  <Minus size={22} aria-hidden="true" />
                </button>
                <input
                  id="quantidade"
                  inputMode="decimal"
                  value={formulario.quantidade}
                  onChange={(evento) =>
                    setFormulario({ ...formulario, quantidade: evento.target.value.replace(/[^\d,.]/g, '') })
                  }
                  aria-invalid={campos.quantidadeKg ? true : undefined}
                  required
                />
                <button type="button" onClick={() => alterarQuantidade(1)} aria-label="Aumentar 1 kg">
                  <Plus size={22} aria-hidden="true" />
                </button>
              </div>
              <div className="atalhos" role="group" aria-label="Quantidades comuns">
                {QUANTIDADES_RAPIDAS.map((valor) => (
                  <button
                    key={valor}
                    type="button"
                    className="atalho"
                    aria-pressed={formulario.quantidade === String(valor)}
                    onClick={() => setFormulario({ ...formulario, quantidade: String(valor) })}
                  >
                    {valor} kg
                  </button>
                ))}
              </div>
              {campos.quantidadeKg && <p className="campo-erro">{frase(campos.quantidadeKg)}</p>}
            </div>

            <Escolha<number>
              legenda="A cozinha pode buscar até"
              valor={formulario.prazoMinutos}
              aoMudar={(prazoMinutos) => setFormulario({ ...formulario, prazoMinutos, horarioPersonalizado: '' })}
              opcoes={PRAZOS}
              compacta
            />
            <Campo
              rotulo="Ou escolha o horário"
              type="time"
              value={formulario.horarioPersonalizado}
              onChange={(evento) =>
                setFormulario({
                  ...formulario,
                  horarioPersonalizado: evento.target.value,
                  prazoMinutos: evento.target.value ? null : 120,
                })
              }
            />

            <Aviso tipo="erro" mensagem={erroFormulario} />
            <Aviso tipo="sucesso" mensagem={sucesso} />

            <button className="botao botao-primario botao-largo" type="submit" disabled={publicando}>
              {publicando ? 'Publicando...' : 'Publicar oferta'}
            </button>
          </form>
        </section>

        <section aria-labelledby="titulo-ativas">
          <h2 id="titulo-ativas">Ofertas de hoje</h2>
          <Aviso tipo="erro" mensagem={erroLista} />
          {ofertas === null && !erroLista && <p className="carregando">Carregando suas ofertas...</p>}
          {ofertas !== null && ativas.length === 0 && (
            <p className="vazio">Nenhuma oferta aberta. Quando sobrar algo, publique ao lado.</p>
          )}
          <div className="lista-placas">
            {ativas.map((oferta) => {
              const reserva = oferta.reserva
              const retirada = reserva?.retirada
              return (
                <PlacaOferta key={oferta.id} oferta={oferta} mostrarBarraca={false}>
                  {oferta.status === 'DISPONIVEL' && (
                    <>
                      <p className="placa-mensagem">Aguardando uma cozinha reservar.</p>
                      <a
                        className="botao botao-secundario"
                        href={`https://wa.me/?text=${encodeURIComponent(mensagemCompartilhamento(oferta))}`}
                        target="_blank"
                        rel="noreferrer"
                      >
                        <Share2 size={18} aria-hidden="true" />
                        Avisar no WhatsApp
                      </a>
                    </>
                  )}
                  {reserva && (
                    <>
                      <p className="placa-mensagem">
                        Reservada por <strong>{reserva.cozinhaNome}</strong>, {quando(reserva.reservadaEm)}.
                        Quem vem buscar: {reserva.responsavel}.
                      </p>
                      <a className="botao botao-secundario" href={telefoneParaLink(reserva.telefone)}>
                        <Phone size={18} aria-hidden="true" />
                        Ligar {telefoneLegivel(reserva.telefone)}
                      </a>
                      {retirada?.confirmadoFeiranteEm ? (
                        <p className="placa-mensagem">Entrega confirmada. Falta a cozinha informar o peso.</p>
                      ) : (
                        <button
                          type="button"
                          className="botao botao-primario"
                          disabled={confirmando === oferta.id}
                          onClick={() => void confirmarEntrega(oferta)}
                        >
                          {confirmando === oferta.id ? 'Confirmando...' : 'Confirmar que entreguei'}
                        </button>
                      )}
                    </>
                  )}
                </PlacaOferta>
              )
            })}
          </div>

          {encerradas.length > 0 && (
            <>
              <h2 className="titulo-historico">Histórico</h2>
              <ul className="historico">
                {encerradas.map((oferta) => (
                  <li key={oferta.id}>
                    <div>
                      <p className="historico-titulo">{oferta.descricao}</p>
                      <p className="historico-detalhe">
                        {oferta.status === 'RETIRADA'
                          ? `${kg(oferta.reserva?.retirada?.pesoKg ?? oferta.quantidadeKg)} kg para ${oferta.reserva?.cozinhaNome}`
                          : `${kg(oferta.quantidadeKg)} kg, ninguém buscou a tempo`}
                        , {quando(oferta.criadaEm)}
                      </p>
                    </div>
                    <button type="button" className="botao-texto" onClick={() => repetir(oferta)}>
                      <RotateCcw size={16} aria-hidden="true" />
                      Repetir
                    </button>
                  </li>
                ))}
              </ul>
            </>
          )}
        </section>
      </div>
    </div>
  )
}
