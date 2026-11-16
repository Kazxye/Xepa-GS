import type {
  CadastroCozinha,
  CadastroFeirante,
  Feira,
  NovaOferta,
  Oferta,
  Painel,
  PerfilUsuario,
  Sessao,
} from './tipos'

const BASE_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  readonly status: number
  readonly campos: Record<string, string>

  constructor(status: number, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem)
    this.name = 'ApiError'
    this.status = status
    this.campos = campos
  }
}

let tokenAtual: string | null = null
let aoExpirarSessao: (() => void) | null = null

export function definirToken(token: string | null) {
  tokenAtual = token
}

export function quandoSessaoExpirar(callback: (() => void) | null) {
  aoExpirarSessao = callback
}

type Metodo = 'GET' | 'POST'

async function requisitar<T>(caminho: string, metodo: Metodo = 'GET', corpo?: unknown): Promise<T> {
  const cabecalhos: Record<string, string> = { Accept: 'application/json' }
  if (corpo !== undefined) {
    cabecalhos['Content-Type'] = 'application/json'
  }
  if (tokenAtual) {
    cabecalhos.Authorization = `Bearer ${tokenAtual}`
  }

  let resposta: Response
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new ApiError(0, 'Sem conexão com o servidor. Confira a internet e tente de novo.')
  }

  if (resposta.status === 401 && tokenAtual) {
    aoExpirarSessao?.()
  }

  const texto = await resposta.text()
  const dados = texto ? JSON.parse(texto) : null

  if (!resposta.ok) {
    const mensagem = dados?.detail ?? 'Não foi possível concluir a ação. Tente de novo.'
    throw new ApiError(resposta.status, mensagem, dados?.campos ?? {})
  }
  return dados as T
}

export const api = {
  feiras: () => requisitar<Feira[]>('/api/feiras'),
  painel: () => requisitar<Painel>('/api/painel'),
  entrar: (email: string, senha: string) => requisitar<Sessao>('/api/auth/login', 'POST', { email, senha }),
  cadastrarFeirante: (dados: CadastroFeirante) => requisitar<Sessao>('/api/auth/cadastro/feirante', 'POST', dados),
  cadastrarCozinha: (dados: CadastroCozinha) => requisitar<Sessao>('/api/auth/cadastro/cozinha', 'POST', dados),
  perfil: () => requisitar<PerfilUsuario>('/api/auth/me'),
  publicarOferta: (dados: NovaOferta) => requisitar<Oferta>('/api/ofertas', 'POST', dados),
  minhasOfertas: () => requisitar<Oferta[]>('/api/ofertas/minhas'),
  confirmarEntrega: (id: number) => requisitar<Oferta>(`/api/ofertas/${id}/entrega`, 'POST'),
  ofertasProximas: () => requisitar<Oferta[]>('/api/ofertas/proximas'),
  ofertasReservadas: () => requisitar<Oferta[]>('/api/ofertas/reservadas'),
  reservar: (id: number) => requisitar<Oferta>(`/api/ofertas/${id}/reserva`, 'POST'),
  confirmarRetirada: (id: number, pesoKg: number) =>
    requisitar<Oferta>(`/api/ofertas/${id}/retirada`, 'POST', { pesoKg }),
}

export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ApiError) {
    return erro.message
  }
  return 'Algo deu errado. Tente de novo.'
}
