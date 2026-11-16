import { createContext, useContext } from 'react'
import type { PerfilUsuario, Sessao } from '../api/tipos'

export interface ValorSessao {
  sessao: Sessao | null
  usuario: PerfilUsuario | null
  iniciar: (sessao: Sessao) => void
  encerrar: () => void
}

export const SessaoContext = createContext<ValorSessao | null>(null)

export function useSessao(): ValorSessao {
  const contexto = useContext(SessaoContext)
  if (!contexto) {
    throw new Error('useSessao precisa estar dentro de SessaoProvider')
  }
  return contexto
}

export function rotaInicial(usuario: PerfilUsuario | null): string {
  if (!usuario) {
    return '/'
  }
  return usuario.perfil === 'FEIRANTE' ? '/feirante' : '/cozinha'
}
