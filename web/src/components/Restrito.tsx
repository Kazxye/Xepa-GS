import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import type { Perfil } from '../api/tipos'
import { rotaInicial, useSessao } from '../auth/sessao'

export function Restrito({ perfil, children }: { perfil: Perfil; children: ReactNode }) {
  const { usuario } = useSessao()
  const local = useLocation()

  if (!usuario) {
    return <Navigate to="/entrar" replace state={{ voltarPara: local.pathname }} />
  }
  if (usuario.perfil !== perfil) {
    return <Navigate to={rotaInicial(usuario)} replace />
  }
  return <>{children}</>
}
