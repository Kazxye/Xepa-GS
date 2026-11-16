export type Perfil = 'FEIRANTE' | 'COZINHA'
export type TipoAlimento = 'FRUTA' | 'LEGUME' | 'VERDURA'
export type StatusOferta = 'DISPONIVEL' | 'RESERVADA' | 'RETIRADA' | 'EXPIRADA'
export type DiaSemana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'

export interface Feira {
  id: number
  nome: string
  endereco: string
  latitude: number
  longitude: number
  diaSemana: DiaSemana
}

export interface DadosFeirante {
  id: number
  nome: string
  telefone: string
  nomeBarraca: string
  feira: Feira
}

export interface DadosCozinha {
  id: number
  nome: string
  responsavel: string
  telefone: string
  endereco: string
  latitude: number
  longitude: number
  raioBuscaKm: number
  capacidadeCargaKg: number
}

export interface PerfilUsuario {
  usuarioId: number
  email: string
  perfil: Perfil
  nome: string
  feirante?: DadosFeirante
  cozinha?: DadosCozinha
}

export interface Sessao {
  token: string
  expiraEm: string
  perfil: PerfilUsuario
}

export interface Retirada {
  pesoKg?: number
  confirmadoFeiranteEm?: string
  confirmadoCozinhaEm?: string
  concluidaEm?: string
}

export interface Reserva {
  cozinhaId: number
  cozinhaNome: string
  responsavel: string
  telefone: string
  reservadaEm: string
  retirada?: Retirada
}

export interface Oferta {
  id: number
  tipo: TipoAlimento
  descricao: string
  quantidadeKg: number
  horarioLimite: string
  fotoUrl?: string
  status: StatusOferta
  criadaEm: string
  distanciaKm?: number
  feirante: { id: number; nome: string; nomeBarraca: string; telefone: string }
  feira: { id: number; nome: string; endereco: string }
  reserva?: Reserva
}

export interface IndicadorFeira {
  feiraId: number
  feiraNome: string
  quilosRecuperados: number
  retiradasConcluidas: number
  ofertasPublicadas: number
}

export interface Painel {
  quilosRecuperados: number
  retiradasConcluidas: number
  cozinhasAtendidas: number
  feirantesDoadores: number
  ofertasPublicadas: number
  taxaDeRetiradaPercentual: number
  tempoMedioAteReservaMinutos?: number
  porFeira: IndicadorFeira[]
}

export interface CadastroFeirante {
  nome: string
  email: string
  senha: string
  telefone: string
  nomeBarraca: string
  feiraId: number | null
}

export interface CadastroCozinha {
  nome: string
  responsavel: string
  email: string
  senha: string
  telefone: string
  endereco: string
  latitude: number | null
  longitude: number | null
  raioBuscaKm: number
  capacidadeCargaKg: number | null
}

export interface NovaOferta {
  tipo: TipoAlimento
  descricao: string
  quantidadeKg: number
  horarioLimite: string
}
