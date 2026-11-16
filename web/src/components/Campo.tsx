import { useId } from 'react'
import type { InputHTMLAttributes, ReactNode } from 'react'
import { frase } from '../lib/formato'

interface CampoProps extends InputHTMLAttributes<HTMLInputElement> {
  rotulo: string
  ajuda?: ReactNode
  erro?: string
}

export function Campo({ rotulo, ajuda, erro, ...entrada }: CampoProps) {
  const id = useId()
  const idAjuda = `${id}-ajuda`
  const idErro = `${id}-erro`
  const descritores = [ajuda ? idAjuda : null, erro ? idErro : null].filter(Boolean).join(' ')

  return (
    <div className={`campo${erro ? ' campo-com-erro' : ''}`}>
      <label htmlFor={id}>{rotulo}</label>
      <input id={id} aria-invalid={erro ? true : undefined} aria-describedby={descritores || undefined} {...entrada} />
      {ajuda && (
        <p className="campo-ajuda" id={idAjuda}>
          {ajuda}
        </p>
      )}
      {erro && (
        <p className="campo-erro" id={idErro}>
          {frase(erro)}
        </p>
      )}
    </div>
  )
}
