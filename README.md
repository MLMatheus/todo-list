# Todo List — Gerenciador de Tarefas

Serviço REST para gerenciamento de tarefas de usuários autenticados via Google.
Cada usuário cadastra, lista (com filtros e paginação), edita, conclui/reabre e exclui as
suas próprias tarefas.

Especificação completa em [`specs/001-task-manager/`](specs/001-task-manager/).

## Stack

- Java 21, Spring Boot 3.5 (Web, Data JPA, Validation, OAuth2 Resource Server)
- MySQL 8 + Flyway (migrations)
- Filtros dinâmicos com JPA Criteria/Specifications
- Autenticação: JWT do Google (assinatura, expiração e audiência)
- Documentação: springdoc-openapi (Swagger UI)
- Logs estruturados em JSON
- Testes: JUnit 5 + Mockito (unitário) e Testcontainers (integração); cobertura 100% (JaCoCo)

## Arquitetura (DDD)

Domínio puro separado da persistência:

- `service/model` — domínio puro (POJOs): agregado `Tarefa`, `Usuario`, VOs `Status`/`Prioridade`
- `service/repository` — portas de domínio + adaptadores (`impl`), entidades JPA (`entity`),
  Spring Data (`jpa`) e Specifications (`spec`)
- `service/impl` — serviços de aplicação (dependem das portas, nunca de JPA)
- `controller` — REST (`contract` + implementação)
- `configuration` — segurança, OpenAPI, clock
- `infrastructure` — utilitários, constantes, filtro de log, tratamento de erros

## Como rodar

### Tudo via Docker Compose (app + MySQL)

```bash
GOOGLE_AUDIENCE=seu-client-id.apps.googleusercontent.com docker compose --profile full up --build
# API em http://localhost:8080/todo-list/v1
```

### Apenas o MySQL (rodar a app pela IDE)

```bash
docker compose --profile mysql up -d
# Suba a aplicação pela IDE apontando para localhost:3306 (db: todolist, user/pass: todo/todo)
```

## Configuração (variáveis de ambiente)

| Variável | Descrição | Default |
|----------|-----------|---------|
| `DB_URL` | URL JDBC do MySQL | `jdbc:mysql://localhost:3306/todolist` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais | `todo` / `todo` |
| `GOOGLE_ISSUER_URI` | Issuer do Google | `https://accounts.google.com` |
| `GOOGLE_AUDIENCE` | Client id (validação de `aud`) | — |
| `INSTANCE_ID` | Id da instância (logs) | `todo-list-local` |

## Build e testes

```bash
# Compilar
mvn clean compile

# Testes unitários + gate de cobertura 100% (JaCoCo)
mvn clean verify

# Testes de integração (Testcontainers MySQL — requer Docker)
mvn verify -P integration
```

> **Docker Engine 29+**: requer **Testcontainers ≥ 2.0.2** (este projeto usa 2.0.5). Versões
> anteriores (docker-java antigo) falham o handshake com a API do Docker 29 (mínimo API 1.44)
> com `Status 400 / Could not find a valid Docker environment`. Já está resolvido aqui.

## Documentação da API

- Swagger UI: `http://localhost:8080/todo-list/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/todo-list/v1/v3/api-docs`
- Contrato versionado: [`specs/001-task-manager/contracts/openapi.yaml`](specs/001-task-manager/contracts/openapi.yaml)
- Coleção de requests (httpyac): [`requests/tarefas.http`](requests/tarefas.http)

## Endpoints (base `/todo-list/v1`)

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/tarefas` | Cria tarefa (201) |
| GET | `/tarefas` | Lista com filtros (`status`, `prioridade`, `data_vencimento`) e paginação |
| PATCH | `/tarefas/{id}` | Atualiza conteúdo e/ou `status` (concluir/reabrir) |
| DELETE | `/tarefas/{id}` | Exclui (204) |

Prioridade: `1=ALTA, 2=MÉDIA, 3=BAIXA` (default `2`). Status: `PENDENTE`, `CONCLUIDA`.
