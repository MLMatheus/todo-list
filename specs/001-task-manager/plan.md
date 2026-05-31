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
| I. DDD | Domínio puro (POJOs sem framework) em `service/model`, com Linguagem Ubíqua (Tarefa, Usuario, Status, Prioridade); persistência isolada em entidades JPA (`service/repository/entity`) acessadas por portas (`*Repository`) + adaptadores; 1 bounded context | ✅ PASS |
| II. TDD (NÃO NEGOCIÁVEL) | Todo código de produção precedido por teste que falha (Red-Green-Refactor); tasks ordenam teste antes da implementação | ✅ PASS |
| III. Cobertura 100% (NÃO NEGOCIÁVEL) | JaCoCo com regra de 100% (linha + branch); exclusões explícitas e justificadas | ✅ PASS |
| IV. SOLID | Contratos por interface (`ITodoListController`, portas `*Repository`, `*Service`/`impl`), Inversão de Dependência via Spring DI (serviços dependem de portas de domínio, não de JPA), ISP por DTOs e interfaces enxutas | ✅ PASS |

**Conclusão inicial**: PASS, sem ressalvas. O domínio é mantido independente do framework de
persistência (decisão D1 do replanejamento). Nenhuma violação. Reavaliar após Phase 1.

**Reavaliação pós-Phase 1 (Design & Contracts)**: Os artefatos `data-model.md`, `contracts/openapi.yaml`
e `quickstart.md` mantêm os gates. O domínio puro expõe regras de negócio em métodos do agregado
(`concluir`, `reabrir`, transições de `Status`); a persistência fica nos adaptadores de
repositório e os filtros usam `Specification` sobre as entidades JPA na camada de infraestrutura.
Sem violações. **PASS**.

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
│   ├── impl/                             # Implementações dos serviços (dependem das PORTAS)
│   ├── mapper/
│   │   ├── TarefaMapper.java             # MapStruct: domínio <-> DTO
│   │   └── TarefaPersistenceMapper.java  # MapStruct: domínio <-> entidade JPA
│   ├── model/                            # DOMÍNIO PURO (sem anotações de framework)
│   │   ├── Tarefa.java                   # Agregado Tarefa (regras: criar/concluir/reabrir/editar)
│   │   ├── Usuario.java                  # Entidade de domínio Usuario
│   │   ├── Status.java                   # Value object (PENDENTE, CONCLUIDA)
│   │   └── Prioridade.java               # Value object (1=ALTA,2=MÉDIA,3=BAIXA)
│   ├── repository/                       # PORTAS de domínio + adaptadores JPA (infra)
│   │   ├── TarefaRepository.java         # Porta (interface em termos de domínio)
│   │   ├── UsuarioRepository.java        # Porta
│   │   ├── entity/
│   │   │   ├── TarefaEntity.java         # Entidade JPA (persistência)
│   │   │   └── UsuarioEntity.java        # Entidade JPA (persistência)
│   │   ├── jpa/
│   │   │   ├── TarefaJpaRepository.java  # Spring Data + JpaSpecificationExecutor<TarefaEntity>
│   │   │   └── UsuarioJpaRepository.java
│   │   ├── spec/
│   │   │   └── TarefaSpecification.java   # CriteriaBuilder sobre TarefaEntity (filtros)
│   │   └── impl/
│   │       ├── TarefaRepositoryImpl.java  # Adaptador porta->JPA (+ persistence mapper)
│   │       └── UsuarioRepositoryImpl.java
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

**Structure Decision**: Single project backend, mantendo o layout de pastas fornecido pelo
usuário (`configuration`/`controller`/`infrastructure`/`service`), mas com **separação estrita
de domínio e persistência** (decisão D1 do replanejamento, para cumprir o Princípio I sem
ressalvas): `service/model` = **domínio puro** (agregado `Tarefa`, `Usuario`, value objects
`Status`/`Prioridade`) sem qualquer anotação de framework; `service/repository` = **portas** de
domínio (`TarefaRepository`, `UsuarioRepository`) com **adaptadores** JPA em `repository/impl`,
entidades de persistência em `repository/entity`, interfaces Spring Data em `repository/jpa` e
specifications em `repository/spec`. Os serviços (`service/impl`) dependem das portas, nunca de
JPA. Mapeamento domínio↔entidade via `TarefaPersistenceMapper` e domínio↔DTO via `TarefaMapper`.

## Complexity Tracking

> O Constitution Check passa sem violações. A separação domínio/persistência adiciona uma
> camada de mapeamento (portas + adaptadores + persistence mapper), aceita deliberadamente
> para honrar o Princípio I (DDD — domínio independente de framework). Não há desvios a
> justificar nesta tabela.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — (nenhuma) | — | — |

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
3. **Filtro por `data_vencimento`**: parâmetro de **igualdade** por data única (`LocalDate`) no
   `GET` (decisão F1 do replanejamento). Filtro por intervalo está fora do escopo desta versão.
4. **Provisão do `Usuario`**: usuário é resolvido/provisionado a partir das claims do token
   Google (`sub`/`email`/`name`) no primeiro acesso; `usuario_id` da tarefa referencia esse
   registro. Garante o isolamento por proprietário (FR-002).
