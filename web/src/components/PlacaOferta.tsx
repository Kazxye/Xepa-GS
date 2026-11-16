import { Apple, Carrot, Clock, Leaf, MapPin, Store } from 'lucide-react'
import type { ReactNode } from 'react'
import type { Oferta, TipoAlimento } from '../api/tipos'
import { NOME_STATUS, NOME_TIPO, distancia, horario, kg, prazo } from '../lib/formato'

const ICONE_TIPO: Record<TipoAlimento, typeof Apple> = {
  FRUTA: Apple,
  LEGUME: Carrot,
  VERDURA: Leaf,
}

interface PlacaOfertaProps {
  oferta: Oferta
  mostrarBarraca?: boolean
  children?: ReactNode
}

export function PlacaOferta({ oferta, mostrarBarraca = true, children }: PlacaOfertaProps) {
  const Icone = ICONE_TIPO[oferta.tipo]
  const ativa = oferta.status === 'DISPONIVEL' || oferta.status === 'RESERVADA'
  const pesoRetirado = oferta.reserva?.retirada?.pesoKg

  return (
    <article className={`placa placa-${oferta.status.toLowerCase()}`}>
      <div className="placa-topo">
        <span className="placa-tipo">
          <Icone size={18} aria-hidden="true" />
          {NOME_TIPO[oferta.tipo]}
        </span>
        <span className="placa-status">{NOME_STATUS[oferta.status]}</span>
      </div>

      <p className="placa-peso">
        {kg(oferta.status === 'RETIRADA' && pesoRetirado ? pesoRetirado : oferta.quantidadeKg)}
        <span>kg</span>
      </p>
      <h3 className="placa-descricao">{oferta.descricao}</h3>

      <ul className="placa-detalhes">
        {mostrarBarraca && (
          <li>
            <Store size={16} aria-hidden="true" />
            <span>
              {oferta.feirante.nomeBarraca}, {oferta.feira.nome}
            </span>
          </li>
        )}
        <li>
          <Clock size={16} aria-hidden="true" />
          <span>
            Retirar até {horario(oferta.horarioLimite)}
            {ativa && <strong className="placa-prazo"> ({prazo(oferta.horarioLimite)})</strong>}
          </span>
        </li>
        {oferta.distanciaKm !== undefined && (
          <li>
            <MapPin size={16} aria-hidden="true" />
            <span>A {distancia(oferta.distanciaKm)} da cozinha</span>
          </li>
        )}
      </ul>

      {children && <div className="placa-acoes">{children}</div>}
    </article>
  )
}
