import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import { rotaInicial, useSessao } from '../auth/sessao'
import { Aviso } from '../components/Aviso'
import { Campo } from '../components/Campo'

const CONTAS_DEMONSTRACAO = [
  { rotulo: 'Feirante: Joana, Frutas da Joana', email: 'joana@demo.xepa.app' },
  { rotulo: 'Cozinha: Cozinha Comunitária Esperança', email: 'cozinha.esperanca@demo.xepa.app' },
]
const SENHA_DEMONSTRACAO = 'xepa2026'

export function Entrar() {
  const { usuario, iniciar } = useSessao()
  const navegar = useNavigate()
  const local = useLocation()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  if (usuario) {
    return <Navigate to={rotaInicial(usuario)} replace />
  }

  async function entrar(emailInformado: string, senhaInformada: string) {
    setEnviando(true)
    setErro(null)
    try {
      const sessao = await api.entrar(emailInformado, senhaInformada)
      iniciar(sessao)
      const voltarPara = (local.state as { voltarPara?: string } | null)?.voltarPara
      navegar(voltarPara ?? rotaInicial(sessao.perfil), { replace: true })
    } catch (falha) {
      setErro(mensagemDeErro(falha))
      setEnviando(false)
    }
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    void entrar(email, senha)
  }

  return (
    <section className="estreita">
      <h1>Entrar</h1>
      <p>Use o e-mail e a senha que você cadastrou.</p>

      <form className="formulario" onSubmit={enviar} noValidate>
        <Aviso tipo="erro" mensagem={erro} />
        <Campo
          rotulo="E-mail"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(evento) => setEmail(evento.target.value)}
          required
        />
        <Campo
          rotulo="Senha"
          type="password"
          autoComplete="current-password"
          value={senha}
          onChange={(evento) => setSenha(evento.target.value)}
          required
        />
        <button className="botao botao-primario botao-largo" type="submit" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>

      <p className="nota">
        Ainda não tem conta? <Link to="/cadastro">Criar conta</Link>
      </p>

      <aside className="demonstracao" aria-labelledby="titulo-demonstracao">
        <h2 id="titulo-demonstracao">Contas de demonstração</h2>
        <p>Para conhecer o aplicativo sem cadastro. A senha das duas é {SENHA_DEMONSTRACAO}.</p>
        <div className="demonstracao-contas">
          {CONTAS_DEMONSTRACAO.map((conta) => (
            <button
              key={conta.email}
              type="button"
              className="botao botao-secundario"
              disabled={enviando}
              onClick={() => void entrar(conta.email, SENHA_DEMONSTRACAO)}
            >
              Entrar como {conta.rotulo}
            </button>
          ))}
        </div>
      </aside>
    </section>
  )
}
