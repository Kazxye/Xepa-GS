import { LogOut } from 'lucide-react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { rotaInicial, useSessao } from '../auth/sessao'

export function Layout() {
  const { usuario, encerrar } = useSessao()
  const navegar = useNavigate()

  function sair() {
    encerrar()
    navegar('/')
  }

  return (
    <div className="pagina">
      <div className="lona" aria-hidden="true" />
      <header className="cabecalho">
        <div className="cabecalho-conteudo">
          <Link to={rotaInicial(usuario)} className="marca">
            <img src="/favicon.svg" alt="" width={32} height={32} />
            <span>Xepa Solidária</span>
          </Link>
          <nav className="navegacao" aria-label="Principal">
            <NavLink to="/" end>
              Impacto
            </NavLink>
            {usuario ? (
              <>
                <NavLink to={rotaInicial(usuario)}>
                  {usuario.perfil === 'FEIRANTE' ? 'Minha barraca' : 'Minha cozinha'}
                </NavLink>
                <button type="button" className="botao-texto" onClick={sair}>
                  <LogOut size={18} aria-hidden="true" />
                  Sair
                </button>
              </>
            ) : (
              <NavLink to="/entrar">Entrar</NavLink>
            )}
          </nav>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
      <footer className="rodape">
        <p>Xepa Solidária. Projeto acadêmico da Global Solution 2026.2, tema CitizenTech.</p>
      </footer>
    </div>
  )
}
