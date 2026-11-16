# Xepa Solidária API

Backend da Xepa Solidária, aplicativo que liga a sobra de feiras livres a cozinhas comunitárias do mesmo bairro. Entrega da disciplina **Domain Driven Design**.

Java 21, Spring Boot 3.5, Spring Security com JWT, Spring Data JPA, Flyway e PostgreSQL.

## Domínio

O feirante publica uma oferta com o que sobrou e até que horas pode ser retirado. Uma cozinha comunitária dentro do raio de busca reserva a oferta, vai até a barraca e as duas partes confirmam a retirada. O painel público soma os quilos que deixaram de ir para o lixo.

### Linguagem ubíqua

| Termo | Significado |
|---|---|
| Feira | Feira livre com endereço, coordenadas e dia da semana |
| Feirante | Dono de uma barraca em uma feira |
| Cozinha | Cozinha comunitária que recebe doações, com raio de busca e capacidade de carga |
| Oferta | Sobra publicada pelo feirante, com tipo, quantidade e horário limite |
| Reserva | Compromisso de uma cozinha de buscar a oferta |
| Retirada | Confirmação da entrega pelas duas partes, com o peso efetivamente levado |
| Painel | Indicadores públicos do impacto do projeto |

### Agregados

| Agregado | Raiz | Entidades e objetos de valor internos |
|---|---|---|
| Oferta | `Oferta` | `Reserva`, `Retirada` |
| Cozinha | `Cozinha` | `Localizacao` (objeto de valor) |
| Feirante | `Feirante` | |
| Feira | `Feira` | `Localizacao` (objeto de valor) |
| Usuário | `Usuario` | `Perfil` |

`Reserva` e `Retirada` só são criadas e alteradas por meio da `Oferta`, que garante as regras do ciclo de vida.

### Ciclo de vida da oferta

```
DISPONIVEL ──reservar──> RESERVADA ──feirante e cozinha confirmam──> RETIRADA
     │                       │
     └──horário limite──> EXPIRADA <──horário limite + 1h──┘
```

### Regras de negócio

1. O horário limite precisa estar no futuro e em até 24 horas.
2. A quantidade deve ficar entre 0,1 kg e 1000 kg.
3. Só a cozinha cujo raio de busca alcança a feira pode reservar.
4. A primeira cozinha que reserva fica com a oferta. A disputa simultânea é resolvida com bloqueio otimista (`@Version`) e restrição única no banco.
5. Não é possível reservar depois do horário limite.
6. A retirada só é concluída quando o feirante e a cozinha confirmam. A cozinha informa o peso retirado.
7. Ofertas disponíveis expiram no horário limite. Ofertas reservadas têm 1 hora de tolerância. Uma tarefa agendada aplica a expiração a cada minuto.

## Arquitetura

```
src/main/java/br/com/xepasolidaria
├── domain           entidades, objetos de valor, regras e interfaces de repositório
├── application      casos de uso, comandos de entrada e visões de saída
├── infrastructure   persistência, segurança (JWT), configuração e agendamento
└── web              controladores REST e tratamento de erros
```

- O domínio não depende de Spring. As interfaces de repositório (`OfertaRepository`, `CozinhaRepository` etc.) ficam no domínio e são implementadas na infraestrutura pelo Spring Data.
- A aplicação orquestra os casos de uso e devolve visões imutáveis (`record`), sem expor entidades na API.
- A infraestrutura gera o token pela porta `TokenService`, definida na aplicação.

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/cadastro/feirante` | público | Cadastra feirante e devolve a sessão |
| POST | `/api/auth/cadastro/cozinha` | público | Cadastra cozinha e devolve a sessão |
| POST | `/api/auth/login` | público | Autentica e devolve o token |
| GET | `/api/auth/me` | autenticado | Perfil do usuário logado |
| GET | `/api/feiras` | público | Lista as feiras |
| GET | `/api/painel` | público | Indicadores de impacto |
| POST | `/api/ofertas` | feirante | Publica oferta |
| GET | `/api/ofertas/minhas` | feirante | Ofertas do feirante |
| POST | `/api/ofertas/{id}/entrega` | feirante | Confirma a entrega |
| GET | `/api/ofertas/proximas` | cozinha | Ofertas disponíveis dentro do raio, ordenadas por distância |
| GET | `/api/ofertas/reservadas` | cozinha | Ofertas reservadas pela cozinha |
| POST | `/api/ofertas/{id}/reserva` | cozinha | Reserva a oferta |
| POST | `/api/ofertas/{id}/retirada` | cozinha | Confirma a retirada com o peso (`{"pesoKg": 12.5}`) |

Erros seguem o formato Problem Details (RFC 9457), com mensagens em português.

## Como executar

Requisito: JDK 21.

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. Sem configuração extra, a API sobe em `http://localhost:8080` com banco H2 em memória, as migrações do Flyway e os dados de demonstração.

```bash
./mvnw test
```

### Usuários de demonstração

Senha de todos: `xepa2026`

| Perfil | E-mail |
|---|---|
| Feirante | `joana@demo.xepa.app` |
| Feirante | `seu.antonio@demo.xepa.app` |
| Cozinha | `cozinha.esperanca@demo.xepa.app` |
| Cozinha | `panela.cheia@demo.xepa.app` |
| Cozinha | `mesa.vizinhanca@demo.xepa.app` |

## Produção

A imagem é gerada pelo `Dockerfile` e roda com o perfil `prod`, que exige as variáveis abaixo.

| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>/<banco>?sslmode=require` |
| `DB_USERNAME` | usuário do banco |
| `DB_PASSWORD` | senha do banco |
| `JWT_SECRET` | texto aleatório com pelo menos 32 caracteres |
| `CORS_ORIGINS` | `https://xepa-solidaria.vercel.app,https://*.vercel.app` |

A verificação de saúde fica em `/actuator/health`.
