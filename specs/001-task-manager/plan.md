# Implementation Plan: Gerenciador de Tarefas

**Branch**: `001-task-manager` | **Date**: 2026-05-30 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-task-manager/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Serviço REST de gerenciamento de tarefas para usuários autenticados via Google. Um usuário
autenticado cria, lista (com filtros e paginação), edita, conclui/reabre e exclui as suas
próprias tarefas. A abordagem técnica usa Java 21 + Spring Boot 3.5, persistência MySQL com
migrations Flyway, filtros dinâmicos via JPA Criteria/Specifications, segurança como OAuth2
Resource Server validando o JWT do Google (assinatura, expiração e audiência), documentação
via springdoc, logs estruturados em JSON e tratamento de erros padronizado. Qualidade guiada
por TDD com 100% de cobertura e testes de integração com Testcontainers.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.5 (Spring Web, Spring Data JPA, Spring Validation,
Spring Security OAuth2 Resource Server), Flyway, MySQL Connector/J, springdoc-openapi
(Swagger UI), Logback + codificador JSON (logstash-logback-encoder), MapStruct (mapper),
JUnit 5, Mockito, Testcontainers (MySQL), JaCoCo

**Storage**: MySQL 8.x; schema versionado com Flyway migrations

**Testing**: JUnit 5 + Mockito (unitário, padrão `mvn test`); Testcontainers MySQL
(integração, Maven profile `integration`); JaCoCo com gate de cobertura 100%

**Target Platform**: Contêiner Linux (Docker) — serviço HTTP backend

**Project Type**: Single project (web service backend)

**Performance Goals**: Sem metas explícitas no escopo; listagem sempre paginada para limitar
volume por resposta. Padrões razoáveis de aplicação web.

**Constraints**:
- `base_url` = `/todo-list/v1`
- Filtros de tarefas construídos com `CriteriaBuilder` + JPA `Specification` (open specification)
- Token Google validado a cada request (assinatura via JWKS do Google, expiração e audiência)
- Logs estruturados em JSON com campos: `id_usuario`, `request_id`, `datetime`
  (`yyyy-mm-dd hh:mm:ss.sss`), `log_level`, `message`, `instance_id`
- Erros padronizados via `GlobalExceptionHandler` (validação, autenticação, erro interno)
- Configuração em `application.yml`
- Requests documentadas em arquivos `.http` (httpyac)
- Docker multi-stage otimizado para produção + `docker-compose` (app + mysql) com flag/profile
  `mysql` para subir apenas o banco

**Scale/Scope**: 1 bounded context (Todo List), 2 entidades (Usuario, Tarefa), 4 endpoints
REST. Escopo pequeno/CRUD com filtros.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derivados de `.specify/memory/constitution.md` (v1.0.0):

| Princípio | Gate | Status |
|-----------|------|--------|
| I. DDD | Domínio modelado com Linguagem Ubíqua (Tarefa, Usuario, Status, Prioridade); 1 bounded context; acesso a infra via abstrações (repositórios) | ⚠ PASS com ressalva — ver Complexity Tracking (entidades JPA usadas como modelo de domínio) |
| II. TDD (NÃO NEGOCIÁVEL) | Todo código de produção precedido por teste que falha (Red-Green-Refactor); tasks ordenam teste antes da implementação | ✅ PASS |
| III. Cobertura 100% (NÃO NEGOCIÁVEL) | JaCoCo com regra de 100% (linha + branch); exclusões explícitas e justificadas | ✅ PASS |
| IV. SOLID | Contratos por interface (`ITodoListController`, `*Repository`, `*Service`/`impl`), Inversão de Dependência via Spring DI, ISP por DTOs e interfaces enxutas | ✅ PASS |

**Conclusão inicial**: PASS com 1 ressalva justificada (Complexity Tracking). Nenhuma violação
bloqueante. Reavaliar após Phase 1.

**Reavaliação pós-Phase 1 (Design & Contracts)**: Os artefatos `data-model.md`, `contracts/openapi.yaml`
e `quickstart.md` mantêm os gates. O domínio expõe regras de negócio em métodos do agregado
(`concluir`, `reabrir`, transições de `Status`) e os filtros usam abstração de `Specification`.
Sem novas violações. **PASS** (ressalva do princípio I permanece, já justificada).

## Project Structure

### Documentation (this feature)

```text
specs/001-task-manager/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── openapi.yaml
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/github/mlmatheus/todolist/
├── configuration/
│   ├── SecurityConfiguration.java        # OAuth2 Resource Server (Google JWT)
│   └── SwaggerConfiguration.java         # springdoc / OpenAPI metadata
├── controller/
│   ├── contract/
│   │   └── ITodoListController.java      # Interface com anotações springdoc + mapeamentos
│   └── TodoListController.java           # Implementação REST
├── infrastructure/
│   ├── constants/
│   │   ├── Log.java                      # Constantes de logging (chaves JSON)
│   │   └── Geral.java                    # Constantes gerais (base_url, etc.)
│   ├── exception/
│   │   └── handler/                      # GlobalExceptionHandler + exceções de domínio
│   └── util/
│       └── DateFormatterUtils.java       # Formatação yyyy-mm-dd hh:mm:ss.sss
├── service/
│   ├── dto/
│   │   ├── request/                      # CriarTarefaRequest, AtualizarTarefaRequest, filtro
│   │   └── response/                     # TarefaResponse, ErroResponse, PageResponse
│   ├── impl/                             # Implementações dos serviços
│   ├── mapper/
│   │   └── TarefaMapper.java             # MapStruct entity <-> DTO
│   ├── model/
│   │   ├── Tarefa.java                   # Entidade/agregado Tarefa (+ Status, Prioridade)
│   │   └── Usuario.java                  # Entidade Usuario
│   ├── repository/
│   │   ├── TarefaRepository.java         # Spring Data JPA + JpaSpecificationExecutor
│   │   └── UsuarioRepository.java
│   ├── TarefaService.java                # Interface (contrato)
│   ├── TodoListService.java              # Interface de orquestração
│   └── UsuarioService.java               # Interface (resolução/provisão do usuário do token)
└── TodoListApplication.java

src/main/resources/
├── application.yml                       # Configuração da aplicação
├── db/migration/                         # Flyway: V1__create_usuario.sql, V2__create_tarefa.sql
└── logback-spring.xml                    # Logs estruturados em JSON

src/test/java/github/mlmatheus/todolist/  # Testes unitários (mvn test)
src/integrationTest/java/...              # Testes de integração Testcontainers (profile integration)

requests/                                 # Arquivos .http (httpyac) de todas as rotas
Dockerfile                                # Multi-stage, otimizado para produção
docker-compose.yml                        # app + mysql; profile "mysql" para apenas o banco
pom.xml                                   # Maven, profiles: default + integration
```

**Structure Decision**: Single project backend. A estrutura segue EXATAMENTE o layout
fornecido pelo usuário (camadas `configuration`/`controller`/`infrastructure`/`service`),
um estilo em camadas pragmático do ecossistema Spring. Os padrões táticos de DDD são mapeados
sobre essa estrutura: `service/model` = modelo de domínio (agregado `Tarefa`, entidade
`Usuario`, value objects `Status`/`Prioridade`), `service/repository` = portas de
persistência, `service/*Service` + `service/impl` = serviços de domínio/aplicação. Ver
Complexity Tracking para o tradeoff aceito sobre entidades JPA como modelo de domínio.

## Complexity Tracking

> Preenchido porque o Constitution Check tem 1 ressalva ao princípio I (DDD).

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| Entidades JPA (`Tarefa`, `Usuario`) usadas diretamente como modelo de domínio (acoplamento ao framework de persistência), em vez de um domínio puro separado das entidades de persistência | (1) O usuário especificou explicitamente o layout `service/model` com as entidades; (2) bounded context único, pequeno e CRUD-cêntrico; (3) regras de negócio ficam em métodos do agregado `Tarefa` (ex.: `concluir()`, `reabrir()`, validação de transição) + serviços de domínio, preservando SOLID e testabilidade | Um domínio puro + entidades de persistência separadas + mapeamento bidirecional adicionaria uma camada de tradução e duplicação de modelo desproporcional ao tamanho do contexto (YAGNI, princípio de simplicidade da constituição). A independência de infra exigida pelo DDD é preservada onde importa: serviços e controllers dependem de abstrações (`*Repository`, `*Service`) e a lógica de negócio não consulta infra diretamente |

## Notas de reconciliação spec ↔ contrato (decisões registradas)

Estas decisões resolvem lacunas entre a especificação e o brief técnico. Sinalizadas para
revisão do usuário; detalhadas em `research.md`.

1. **Concluir/Reabrir tarefa (FR-008)**: o conjunto de endpoints fornecido não contemplava
   alteração de `status`. Decisão: o corpo do `PATCH /tarefas/{id}` aceita `status` como campo
   **opcional**, permitindo concluir (`CONCLUIDA`) e reabrir (`PENDENTE`) via atualização
   parcial, sem criar novo endpoint. Idempotente conforme FR-009.
2. **`prioridade` como `int`**: a spec descrevia Baixa/Média/Alta; o brief define `int`.
   Decisão: inteiro validado em `1..3` mapeado para o value object `Prioridade`
   (`1=ALTA, 2=MÉDIA, 3=BAIXA` — convenção P1/P2/P3, menor número = maior prioridade;
   default `2`=MÉDIA quando omitido). Mapeamento documentado e validado.
3. **Filtro por `data_vencimento`**: parâmetro de igualdade por data (`LocalDate`) no `GET`,
   coerente com a assumption da spec (filtro pela data de vencimento).
4. **Provisão do `Usuario`**: usuário é resolvido/provisionado a partir das claims do token
   Google (`sub`/`email`/`name`) no primeiro acesso; `usuario_id` da tarefa referencia esse
   registro. Garante o isolamento por proprietário (FR-002).
