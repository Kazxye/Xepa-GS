import { Link } from 'react-router-dom'

export function NaoEncontrada() {
  return (
    <section className="estreita">
      <h1>Página não encontrada</h1>
      <p>O endereço pode ter mudado. Volte para o início e siga pelo menu.</p>
      <Link className="botao botao-primario" to="/">
        Voltar para o início
      </Link>
    </section>
  )
}
