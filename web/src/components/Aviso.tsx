import { CircleAlert, CircleCheck } from 'lucide-react'

interface AvisoProps {
  tipo: 'erro' | 'sucesso'
  mensagem: string | null
}

export function Aviso({ tipo, mensagem }: AvisoProps) {
  if (!mensagem) {
    return null
  }
  const Icone = tipo === 'erro' ? CircleAlert : CircleCheck
  return (
    <div className={`aviso aviso-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      <Icone size={20} aria-hidden="true" />
      <span>{mensagem}</span>
    </div>
  )
}
