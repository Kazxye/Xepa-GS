import { useEffect } from 'react'

export function useAtualizacaoPeriodica(carregar: () => Promise<void>, intervaloMs: number) {
  useEffect(() => {
    const executar = () => void carregar()
    const inicial = window.setTimeout(executar, 0)
    const periodico = window.setInterval(executar, intervaloMs)
    return () => {
      window.clearTimeout(inicial)
      window.clearInterval(periodico)
    }
  }, [carregar, intervaloMs])
}
