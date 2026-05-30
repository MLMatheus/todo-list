# Quickstart — Gerenciador de Tarefas

**Feature**: 001-task-manager | **Date**: 2026-05-30

Guia para subir, desenvolver e testar o serviço.

## Pré-requisitos

- Java 21 (JDK)
- Maven 3.9+
- Docker + Docker Compose
- VS Code com a extensão **httpyac** (para os arquivos `.http`)
- Um **client id** do Google (OAuth) para validar a audiência do token

## Configuração

Variáveis principais (em `application.yml` / variáveis de ambiente):

| Config | Descrição | Exemplo |
|--------|-----------|---------|
| `spring.datasource.url` | URL do MySQL | `jdbc:mysql://localhost:3306/todolist` |
| `spring.datasource.username` / `password` | Credenciais | `todo` / `todo` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Issuer do Google | `https://accounts.google.com` |
| `app.security.google.audience` | Client id (validação de `aud`) | `xxxxx.apps.googleusercontent.com` |
| `app.instance-id` | Identificador da instância p/ logs | `todo-list-1` |

## Subir com Docker Compose

### Aplicação + banco (produção/local completo)

```bash
docker compose up --build
# App em http://localhost:8080/todo-list/v1
```

### Apenas o MySQL (rodar a app pela IDE)

```bash
docker compose --profile mysql up -d
# Suba a aplicação pela IDE apontando para o MySQL em localhost:3306
```

> O `Dockerfile` é multi-stage (build com Maven+JDK 21 → runtime com JRE slim, usuário não-root),
> otimizado para produção. O profile `mysql` do Compose limita o `up` apenas ao banco.

## Migrations (Flyway)

As migrations em `src/main/resources/db/migration` são aplicadas automaticamente no startup:

- `V1__create_usuario.sql`
- `V2__create_tarefa.sql`

## Build e testes

```bash
# Build + testes unitários + gate de cobertura 100% (JaCoCo)
mvn clean verify

# Testes de integração (Testcontainers MySQL) — requer Docker
mvn verify -P integration
```

> **TDD**: escreva o teste que falha primeiro (Red), implemente o mínimo (Green), refatore.
> O `mvn verify` falha se a cobertura ficar abaixo de 100% (linha e branch).

## Documentação da API

- Swagger UI: `http://localhost:8080/todo-list/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/todo-list/v1/v3/api-docs`
- Contrato versionado: [`contracts/openapi.yaml`](./contracts/openapi.yaml)

## Requests (.http / httpyac)

Arquivos em `requests/` cobrem todas as rotas. Defina o token e a base URL nas variáveis:

```http
# requests/tarefas.http
@base = http://localhost:8080/todo-list/v1
@token = {{$dotenv GOOGLE_ID_TOKEN}}

### Criar tarefa
POST {{base}}/tarefas
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "titulo": "Estudar DDD",
  "descricao": "Capítulo de agregados",
  "prioridade": 3,
  "data_vencimento": "2026-06-10"
}

### Listar (filtros + paginação)
GET {{base}}/tarefas?status=PENDENTE&prioridade=3&page=0&size=20&sort=data_vencimento,asc
Authorization: Bearer {{token}}

### Concluir (status no PATCH)
PATCH {{base}}/tarefas/{{tarefaId}}
Authorization: Bearer {{token}}
Content-Type: application/json

{ "status": "CONCLUIDA" }

### Excluir
DELETE {{base}}/tarefas/{{tarefaId}}
Authorization: Bearer {{token}}
```

## Fluxo de validação manual (smoke)

1. Suba o ambiente (`docker compose up --build`).
2. Obtenha um ID token válido do Google e exporte como `GOOGLE_ID_TOKEN`.
3. `POST /tarefas` → espera `201` com a tarefa em `PENDENTE`.
4. `GET /tarefas?status=PENDENTE` → a tarefa aparece na página.
5. `PATCH /tarefas/{id}` com `{"status":"CONCLUIDA"}` → espera `200`, status `CONCLUIDA`.
6. `GET /tarefas?status=CONCLUIDA` → a tarefa aparece; em `?status=PENDENTE` não aparece.
7. `DELETE /tarefas/{id}` → espera `204`; a tarefa some das listagens.
8. Repita um `PATCH`/`DELETE` com token de outro usuário → espera `404` (isolamento por dono).
