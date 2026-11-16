# Xepa Solidária Web

Aplicativo web da Xepa Solidária. Feirantes avisam a sobra do fim da feira e cozinhas comunitárias do bairro reservam e buscam no mesmo dia.

React 19, TypeScript, Vite e React Router. Funciona no navegador do celular e do computador.

## Telas

| Rota | Quem usa | O que faz |
|---|---|---|
| `/` | todos | Painel público com os quilos recuperados, resultados do piloto e ranking por feira |
| `/entrar` | todos | Login, com atalhos para as contas de demonstração |
| `/cadastro` | todos | Cadastro de feirante ou de cozinha |
| `/feirante` | feirante | Publicar sobra, acompanhar reservas, confirmar entrega e repetir ofertas antigas |
| `/cozinha` | cozinha | Ofertas dentro do raio, reserva, confirmação da retirada com o peso e carga da viagem |

As listas do feirante e da cozinha se atualizam a cada 30 segundos.

## Como executar

Requisito: Node.js 20 ou superior e a API rodando.

```bash
npm install
npm run dev
```

O endereço da API vem da variável `VITE_API_URL` (veja `.env.example`). Sem ela, o app usa `http://localhost:8080`.

```bash
npm run build
npm run lint
```

## Deploy na Vercel

1. Importe o repositório na Vercel e defina **Root Directory** como `web`.
2. Em **Environment Variables**, crie `VITE_API_URL` com o endereço da API publicada na Render.
3. O arquivo `vercel.json` redireciona todas as rotas para o `index.html`.

Na API, inclua o domínio da Vercel em `CORS_ORIGINS`.
