import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError, api, mensagemDeErro } from '../api/cliente'
import type { CadastroCozinha, CadastroFeirante, Feira, Perfil } from '../api/tipos'
import { rotaInicial, useSessao } from '../auth/sessao'
import { Aviso } from '../components/Aviso'
import { Campo } from '../components/Campo'
import { Escolha } from '../components/Escolha'
import { NOME_DIA } from '../lib/formato'

const CENTRO_DO_PILOTO = { latitude: -23.5895, longitude: -46.6365 }

const RAIOS = [0.5, 1, 1.5, 2, 3, 5].map((km) => ({
  valor: km,
  rotulo: km < 1 ? `${km * 1000} m` : `${String(km).replace('.', ',')} km`,
}))

type OrigemLocalizacao = 'piloto' | 'aparelho'

export function Cadastro() {
  const { usuario, iniciar } = useSessao()
  const navegar = useNavigate()
  const [parametros] = useSearchParams()
  const [perfil, setPerfil] = useState<Perfil>(
    parametros.get('perfil') === 'cozinha' ? 'COZINHA' : 'FEIRANTE',
  )
  const [feiras, setFeiras] = useState<Feira[]>([])
  const [comum, setComum] = useState({ nome: '', email: '', senha: '', telefone: '' })
  const [feirante, setFeirante] = useState({ nomeBarraca: '', feiraId: '' })
  const [cozinha, setCozinha] = useState({ responsavel: '', endereco: '', raioBuscaKm: 1.5, capacidadeCargaKg: '40' })
  const [origem, setOrigem] = useState<OrigemLocalizacao>('piloto')
  const [coordenadas, setCoordenadas] = useState(CENTRO_DO_PILOTO)
  const [localizando, setLocalizando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})

  useEffect(() => {
    api
      .feiras()
      .then(setFeiras)
      .catch((falha) => setErro(mensagemDeErro(falha)))
  }, [])

  if (usuario) {
    return <Navigate to={rotaInicial(usuario)} replace />
  }

  function usarLocalizacaoDoAparelho() {
    if (!('geolocation' in navigator)) {
      setErro('Este aparelho não informa a localização. Use a área do piloto.')
      return
    }
    setLocalizando(true)
    navigator.geolocation.getCurrentPosition(
      (posicao) => {
        setCoordenadas({ latitude: posicao.coords.latitude, longitude: posicao.coords.longitude })
        setOrigem('aparelho')
        setLocalizando(false)
      },
      () => {
        setErro('Não foi possível obter a localização. Permita o acesso no navegador ou use a área do piloto.')
        setOrigem('piloto')
        setCoordenadas(CENTRO_DO_PILOTO)
        setLocalizando(false)
      },
      { enableHighAccuracy: true, timeout: 10_000 },
    )
  }

  function escolherOrigem(valor: OrigemLocalizacao) {
    setErro(null)
    if (valor === 'aparelho') {
      usarLocalizacaoDoAparelho()
    } else {
      setOrigem('piloto')
      setCoordenadas(CENTRO_DO_PILOTO)
    }
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setEnviando(true)
    setErro(null)
    setCampos({})
    try {
      const sessao =
        perfil === 'FEIRANTE'
          ? await api.cadastrarFeirante({
              ...comum,
              nomeBarraca: feirante.nomeBarraca,
              feiraId: feirante.feiraId ? Number(feirante.feiraId) : null,
            } satisfies CadastroFeirante)
          : await api.cadastrarCozinha({
              ...comum,
              responsavel: cozinha.responsavel,
              endereco: cozinha.endereco,
              latitude: coordenadas.latitude,
              longitude: coordenadas.longitude,
              raioBuscaKm: cozinha.raioBuscaKm,
              capacidadeCargaKg: cozinha.capacidadeCargaKg ? Number(cozinha.capacidadeCargaKg) : null,
            } satisfies CadastroCozinha)
      iniciar(sessao)
      navegar(rotaInicial(sessao.perfil), { replace: true })
    } catch (falha) {
      setErro(mensagemDeErro(falha))
      if (falha instanceof ApiError) {
        setCampos(falha.campos)
      }
      setEnviando(false)
    }
  }

  const ehFeirante = perfil === 'FEIRANTE'

  return (
    <section className="estreita">
      <h1>Criar conta</h1>
      <p>Leva menos de dois minutos. Depois disso você já pode publicar ou reservar ofertas.</p>

      <form className="formulario" onSubmit={enviar} noValidate>
        <Escolha<Perfil>
          legenda="Quem vai usar a conta"
          valor={perfil}
          aoMudar={(valor) => {
            setPerfil(valor)
            setCampos({})
            setErro(null)
          }}
          opcoes={[
            { valor: 'FEIRANTE', rotulo: 'Tenho uma barraca' },
            { valor: 'COZINHA', rotulo: 'Sou de uma cozinha' },
          ]}
        />

        <Aviso tipo="erro" mensagem={erro} />

        <Campo
          rotulo={ehFeirante ? 'Seu nome' : 'Nome da cozinha'}
          value={comum.nome}
          onChange={(evento) => setComum({ ...comum, nome: evento.target.value })}
          autoComplete={ehFeirante ? 'name' : 'organization'}
          erro={campos.nome}
          required
        />

        {ehFeirante ? (
          <>
            <Campo
              rotulo="Nome da barraca"
              value={feirante.nomeBarraca}
              onChange={(evento) => setFeirante({ ...feirante, nomeBarraca: evento.target.value })}
              placeholder="Ex.: Frutas da Joana"
              erro={campos.nomeBarraca}
              required
            />
            <div className={`campo${campos.feiraId ? ' campo-com-erro' : ''}`}>
              <label htmlFor="feira">Feira onde você trabalha</label>
              <select
                id="feira"
                value={feirante.feiraId}
                onChange={(evento) => setFeirante({ ...feirante, feiraId: evento.target.value })}
                aria-invalid={campos.feiraId ? true : undefined}
                required
              >
                <option value="">Escolha a feira</option>
                {feiras.map((feira) => (
                  <option key={feira.id} value={feira.id}>
                    {feira.nome} ({NOME_DIA[feira.diaSemana]})
                  </option>
                ))}
              </select>
              {campos.feiraId && <p className="campo-erro">Escolha a feira.</p>}
            </div>
          </>
        ) : (
          <>
            <Campo
              rotulo="Pessoa responsável"
              value={cozinha.responsavel}
              onChange={(evento) => setCozinha({ ...cozinha, responsavel: evento.target.value })}
              autoComplete="name"
              erro={campos.responsavel}
              required
            />
            <Campo
              rotulo="Endereço da cozinha"
              value={cozinha.endereco}
              onChange={(evento) => setCozinha({ ...cozinha, endereco: evento.target.value })}
              autoComplete="street-address"
              placeholder="Rua, número e bairro"
              erro={campos.endereco}
              required
            />
            <Escolha<OrigemLocalizacao>
              legenda="Localização usada para calcular a distância"
              valor={origem}
              aoMudar={escolherOrigem}
              opcoes={[
                { valor: 'piloto', rotulo: 'Área do piloto (Vila Mariana)' },
                { valor: 'aparelho', rotulo: localizando ? 'Localizando...' : 'Onde estou agora' },
              ]}
            />
            <Escolha<number>
              legenda="Até que distância a cozinha consegue buscar"
              valor={cozinha.raioBuscaKm}
              aoMudar={(valor) => setCozinha({ ...cozinha, raioBuscaKm: valor })}
              opcoes={RAIOS}
              compacta
            />
            <Campo
              rotulo="Quantos quilos a equipe carrega por viagem"
              type="number"
              inputMode="decimal"
              min={1}
              max={1000}
              value={cozinha.capacidadeCargaKg}
              onChange={(evento) => setCozinha({ ...cozinha, capacidadeCargaKg: evento.target.value })}
              ajuda="Considere quem vai buscar a pé ou com carrinho."
              erro={campos.capacidadeCargaKg}
              required
            />
          </>
        )}

        <Campo
          rotulo="Telefone com DDD"
          type="tel"
          inputMode="tel"
          autoComplete="tel-national"
          value={comum.telefone}
          onChange={(evento) => setComum({ ...comum, telefone: evento.target.value })}
          placeholder="11 98765-4321"
          ajuda={ehFeirante ? 'A cozinha que reservar vai ver este número.' : 'O feirante vai ver este número.'}
          erro={campos.telefone}
          required
        />
        <Campo
          rotulo="E-mail"
          type="email"
          autoComplete="email"
          value={comum.email}
          onChange={(evento) => setComum({ ...comum, email: evento.target.value })}
          erro={campos.email}
          required
        />
        <Campo
          rotulo="Senha"
          type="password"
          autoComplete="new-password"
          value={comum.senha}
          onChange={(evento) => setComum({ ...comum, senha: evento.target.value })}
          ajuda="Pelo menos 8 caracteres."
          erro={campos.senha}
          required
        />

        <button className="botao botao-primario botao-largo" type="submit" disabled={enviando || localizando}>
          {enviando ? 'Criando conta...' : 'Criar conta'}
        </button>
      </form>

      <p className="nota">
        Já tem conta? <Link to="/entrar">Entrar</Link>
      </p>
    </section>
  )
}
