import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { SessaoProvider } from './auth/SessaoContext'
import { Layout } from './components/Layout'
import { Restrito } from './components/Restrito'
import { Cadastro } from './pages/Cadastro'
import { Cozinha } from './pages/Cozinha'
import { Entrar } from './pages/Entrar'
import { Feirante } from './pages/Feirante'
import { Inicio } from './pages/Inicio'
import { NaoEncontrada } from './pages/NaoEncontrada'

export function App() {
  return (
    <SessaoProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Inicio />} />
            <Route path="entrar" element={<Entrar />} />
            <Route path="cadastro" element={<Cadastro />} />
            <Route
              path="feirante"
              element={
                <Restrito perfil="FEIRANTE">
                  <Feirante />
                </Restrito>
              }
            />
            <Route
              path="cozinha"
              element={
                <Restrito perfil="COZINHA">
                  <Cozinha />
                </Restrito>
              }
            />
            <Route path="*" element={<NaoEncontrada />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </SessaoProvider>
  )
}
