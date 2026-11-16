import type { ReactNode } from 'react'

export interface OpcaoEscolha<T extends string | number> {
  valor: T
  rotulo: string
  icone?: ReactNode
}

interface EscolhaProps<T extends string | number> {
  legenda: string
  opcoes: OpcaoEscolha<T>[]
  valor: T | null
  aoMudar: (valor: T) => void
  compacta?: boolean
}

export function Escolha<T extends string | number>({ legenda, opcoes, valor, aoMudar, compacta }: EscolhaProps<T>) {
  return (
    <fieldset className={`escolha${compacta ? ' escolha-compacta' : ''}`}>
      <legend>{legenda}</legend>
      <div className="escolha-opcoes">
        {opcoes.map((opcao) => (
          <button
            key={opcao.valor}
            type="button"
            className="escolha-opcao"
            aria-pressed={valor === opcao.valor}
            onClick={() => aoMudar(opcao.valor)}
          >
            {opcao.icone}
            {opcao.rotulo}
          </button>
        ))}
      </div>
    </fieldset>
  )
}
