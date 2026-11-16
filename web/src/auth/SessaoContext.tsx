import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { definirToken, quandoSessaoExpirar } from '../api/cliente'
import type { Sessao } from '../api/tipos'
import { SessaoContext } from './sessao'

const CHAVE = 'xepa.sessao'

function lerSessaoSalva(): Sessao | null {
  try {
    const bruto = localStorage.getItem(CHAVE)
    if (!bruto) {
      return null
    }
    const sessao = JSON.parse(bruto) as Sessao
    if (new Date(sessao.expiraEm).getTime() <= Date.now()) {
      localStorage.removeItem(CHAVE)
      return null
    }
    return sessao
  } catch {
    return null
  }
}

function salvarSessao(sessao: Sessao | null) {
  try {
    if (sessao) {
      localStorage.setItem(CHAVE, JSON.stringify(sessao))
    } else {
      localStorage.removeItem(CHAVE)
    }
  } catch {
    return
  }
}

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => {
    const salva = lerSessaoSalva()
    definirToken(salva?.token ?? null)
    return salva
  })

  const iniciar = useCallback((nova: Sessao) => {
    definirToken(nova.token)
    salvarSessao(nova)
    setSessao(nova)
  }, [])

  const encerrar = useCallback(() => {
    definirToken(null)
    salvarSessao(null)
    setSessao(null)
  }, [])

  useEffect(() => {
    quandoSessaoExpirar(encerrar)
    return () => quandoSessaoExpirar(null)
  }, [encerrar])

  const valor = useMemo(
    () => ({ sessao, usuario: sessao?.perfil ?? null, iniciar, encerrar }),
    [sessao, iniciar, encerrar],
  )

  return <SessaoContext.Provider value={valor}>{children}</SessaoContext.Provider>
}
