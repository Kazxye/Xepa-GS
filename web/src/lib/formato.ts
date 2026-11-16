import type { DiaSemana, StatusOferta, TipoAlimento } from '../api/tipos'

const FUSO = 'America/Sao_Paulo'

const numeroKg = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })
const numeroInteiro = new Intl.NumberFormat('pt-BR')
const hora = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: FUSO })
const dia = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', timeZone: FUSO })
const diaChave = new Intl.DateTimeFormat('en-CA', { timeZone: FUSO })

export function kg(valor: number): string {
  return numeroKg.format(valor)
}

export function inteiro(valor: number): string {
  return numeroInteiro.format(valor)
}

export function horario(iso: string): string {
  return hora.format(new Date(iso))
}

export function quando(iso: string): string {
  const data = new Date(iso)
  const hoje = diaChave.format(new Date())
  const ontem = diaChave.format(new Date(Date.now() - 86_400_000))
  const chave = diaChave.format(data)
  if (chave === hoje) {
    return `hoje às ${hora.format(data)}`
  }
  if (chave === ontem) {
    return `ontem às ${hora.format(data)}`
  }
  return `${dia.format(data)} às ${hora.format(data)}`
}

export function distancia(km: number): string {
  if (km < 1) {
    return `${Math.max(10, Math.round((km * 1000) / 10) * 10)} m`
  }
  return `${numeroKg.format(km)} km`
}

export function minutosRestantes(iso: string): number {
  return Math.round((new Date(iso).getTime() - Date.now()) / 60_000)
}

export function prazo(iso: string): string {
  const minutos = minutosRestantes(iso)
  if (minutos <= 0) {
    return 'prazo encerrado'
  }
  if (minutos < 60) {
    return `faltam ${minutos} min`
  }
  const horas = Math.floor(minutos / 60)
  const resto = minutos % 60
  return resto === 0 ? `faltam ${horas} h` : `faltam ${horas} h ${resto} min`
}

export const NOME_TIPO: Record<TipoAlimento, string> = {
  FRUTA: 'Fruta',
  LEGUME: 'Legume',
  VERDURA: 'Verdura',
}

export const NOME_STATUS: Record<StatusOferta, string> = {
  DISPONIVEL: 'Disponível',
  RESERVADA: 'Reservada',
  RETIRADA: 'Retirada',
  EXPIRADA: 'Expirou',
}

export const NOME_DIA: Record<DiaSemana, string> = {
  MONDAY: 'segunda',
  TUESDAY: 'terça',
  WEDNESDAY: 'quarta',
  THURSDAY: 'quinta',
  FRIDAY: 'sexta',
  SATURDAY: 'sábado',
  SUNDAY: 'domingo',
}

export function frase(texto: string): string {
  const limpo = texto.trim()
  if (!limpo) {
    return limpo
  }
  const capitalizado = limpo.charAt(0).toUpperCase() + limpo.slice(1)
  return /[.!?]$/.test(capitalizado) ? capitalizado : `${capitalizado}.`
}

export function telefoneParaLink(telefone: string): string {
  return `tel:+55${telefone.replace(/\D/g, '')}`
}

export function telefoneLegivel(telefone: string): string {
  const digitos = telefone.replace(/\D/g, '')
  if (digitos.length === 11) {
    return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`
  }
  if (digitos.length === 10) {
    return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`
  }
  return telefone
}
