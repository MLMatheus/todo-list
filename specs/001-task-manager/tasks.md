---

description: "Task list for Gerenciador de Tarefas (001-task-manager)"
---

# Tasks: Gerenciador de Tarefas

**Input**: Design documents from `/specs/001-task-manager/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/openapi.yaml

**Tests**: OBRIGATÓRIOS. A constituição do projeto exige TDD (NÃO NEGOCIÁVEL) e cobertura 100%.
Cada tarefa de implementação é precedida por um teste que falha (Red → Green → Refactor).

**Arquitetura (decisão D1)**: domínio puro (`service/model`, POJOs) separado das entidades de
persistência JPA (`service/repository/entity`), conectados por **portas** (`service/repository`)
+ **adaptadores** (`service/repository/impl`) + **persistence mapper**. Serviços dependem das
portas, nunca de JPA.

**Organization**: Tarefas agrupadas por user story, em ordem de prioridade (P1 → P2 → P3).

> **Status de implementação (2026-05-30)**: ✅ COMPLETO. `mvn clean verify` passa com o **gate
> JaCoCo de 100%** (45 testes unitários) e `mvn verify -P integration` passa com **17 testes de
> integração** (Testcontainers MySQL 8.4) — todas as user stories e o 401 cobertos ponta a ponta.
> Causa raiz do problema de Docker resolvida: **Docker Engine 29 (API ≥ 1.44)** é incompatível com
> o docker-java do Testcontainers 1.20.x → **upgrade para Testcontainers 2.0.5** (docker-java 3.7,
> API default 1.44). A integração também revelou e corrigiu um bug de schema (CHAR×VARCHAR no `id`).
> Observação: os testes de integração ficam em `src/test/java/.../*IT.java` (separados dos
> unitários pelo Surefire/Failsafe), e não em `src/integrationTest/java` como nos caminhos abaixo.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências entre si)
- **[Story]**: User story relacionada (US1..US5) — só nas fases de user story
- Caminhos de arquivo são relativos à raiz do repositório

## Path Conventions

- Pacote raiz: `src/main/java/github/mlmatheus/todolist/`
- Testes unitários: `src/test/java/github/mlmatheus/todolist/`
- Testes de integração (profile `integration`): `src/integrationTest/java/github/mlmatheus/todolist/`
- Recursos: `src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialização do projeto, build, qualidade e empacotamento.

- [X] T001 Criar `pom.xml` (Maven) com Java 21 e Spring Boot 3.5 (Web, Data JPA, Validation, OAuth2 Resource Server), Flyway, MySQL Connector/J, springdoc-openapi, logstash-logback-encoder, MapStruct; dependências de teste JUnit 5, Mockito, Testcontainers (mysql)
- [X] T002 [P] Criar `src/main/java/github/mlmatheus/todolist/TodoListApplication.java` (classe de bootstrap Spring Boot)
- [X] T003 [P] Criar `src/main/resources/application.yml` (datasource MySQL, `jpa.hibernate.ddl-auto=validate`, Flyway, `oauth2.resourceserver.jwt.issuer-uri`, `app.security.google.audience`, `app.instance-id`, base-path `/todo-list/v1`)
- [X] T004 [P] Configurar JaCoCo no `pom.xml` com regra de cobertura 100% (linha e branch) no goal `verify`, com exclusões explícitas justificadas (`TodoListApplication`, DTOs/records sem lógica, interfaces de contrato, entidades JPA sem lógica)
- [X] T005 [P] Configurar Maven profile `integration` no `pom.xml` (source set `src/integrationTest/java`, failsafe plugin, ativação do Testcontainers)
- [X] T006 [P] Criar `Dockerfile` multi-stage (build Maven+JDK 21 → runtime JRE slim, usuário não-root, JAR em camadas) otimizado para produção
- [X] T007 [P] Criar `docker-compose.yml` com serviços `app` e `mysql`, incluindo profile `mysql` para subir apenas o banco
- [X] T008 [P] Criar `src/main/resources/logback-spring.xml` com appender JSON (logstash-encoder) e campos `id_usuario`, `request_id`, `datetime` (`yyyy-MM-dd HH:mm:ss.SSS`), `log_level`, `message`, `instance_id`

**Checkpoint**: Projeto compila (`mvn compile`), sobe vazio e o pipeline de cobertura está armado.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Núcleo compartilhado que DEVE existir antes de qualquer user story — domínio puro,
persistência (entidades/ports/adapters), segurança, usuário, erros, observabilidade e contrato.

**⚠️ CRITICAL**: Nenhuma user story pode começar antes desta fase.

### Constantes e utilitários

- [X] T009 [P] Criar `infrastructure/constants/Geral.java` (base_url, constantes gerais)
- [X] T010 [P] Criar `infrastructure/constants/Log.java` (chaves JSON de log)
- [X] T011 [P] Teste unitário `DateFormatterUtilsTest` em `src/test/java/github/mlmatheus/todolist/infrastructure/util/DateFormatterUtilsTest.java` (formato `yyyy-MM-dd HH:mm:ss.SSS`)
- [X] T012 Implementar `infrastructure/util/DateFormatterUtils.java` (faz T011 passar)

### Domínio puro (`service/model`) — sem anotações de framework

- [X] T013 [P] Teste unitário `PrioridadeTest` em `.../service/model/PrioridadeTest.java` (mapeamento int↔enum `1=ALTA,2=MÉDIA,3=BAIXA`; fora de 1..3 inválido)
- [X] T014 Implementar value object `Prioridade` em `src/main/java/github/mlmatheus/todolist/service/model/Prioridade.java` (faz T013 passar)
- [X] T015 [P] Implementar value object/enum `Status` em `.../service/model/Status.java` (`PENDENTE`, `CONCLUIDA`)
- [X] T016 [P] Implementar entidade de domínio `Usuario` (POJO puro) em `.../service/model/Usuario.java`
- [X] T017 Implementar agregado de domínio `Tarefa` (POJO puro: campos + VOs, sem comportamentos de transição ainda) em `.../service/model/Tarefa.java`

### Persistência (entidades JPA + Spring Data) — `service/repository/entity` e `/jpa`

- [X] T018 [P] Criar entidade JPA `UsuarioEntity` em `.../service/repository/entity/UsuarioEntity.java`
- [X] T019 [P] Criar entidade JPA `TarefaEntity` (colunas conforme data-model, `@Enumerated(STRING)` para status) em `.../service/repository/entity/TarefaEntity.java`
- [X] T020 [P] Criar migration `src/main/resources/db/migration/V1__create_usuario.sql` (PK id CHAR(36), nome, email UNIQUE)
- [X] T021 [P] Criar migration `src/main/resources/db/migration/V2__create_tarefa.sql` (colunas + FK usuario_id + índices `idx_tarefa_usuario`, `idx_tarefa_usuario_status`, `idx_tarefa_usuario_venc`)
- [X] T022 [P] Criar `UsuarioJpaRepository` (`JpaRepository<UsuarioEntity,String>`, busca por email) em `.../service/repository/jpa/UsuarioJpaRepository.java`
- [X] T023 [P] Criar `TarefaJpaRepository` (`JpaRepository<TarefaEntity,String>` + `JpaSpecificationExecutor<TarefaEntity>`) em `.../service/repository/jpa/TarefaJpaRepository.java`

### Portas de domínio, persistence mapper e adaptadores

- [X] T024 [P] Criar porta `TarefaRepository` (interface em termos de domínio: salvar, buscarPorIdEUsuario, listar, excluir) em `.../service/repository/TarefaRepository.java`
- [X] T025 [P] Criar porta `UsuarioRepository` (interface de domínio) em `.../service/repository/UsuarioRepository.java`
- [X] T026 [P] Teste unitário `TarefaPersistenceMapperTest` (domínio↔entidade, ida e volta) em `.../service/mapper/TarefaPersistenceMapperTest.java`
- [X] T027 Implementar `TarefaPersistenceMapper` (MapStruct, domínio↔entidade para Tarefa e Usuario) em `.../service/mapper/TarefaPersistenceMapper.java` (faz T026 passar)
- [X] T028 Teste de integração `RepositorioTarefaIT` (Testcontainers: salvar/buscar via porta `TarefaRepository`, valida mapper + JPA + migrations) em `src/integrationTest/java/github/mlmatheus/todolist/RepositorioTarefaIT.java`
- [X] T029 Implementar adaptador `TarefaRepositoryImpl` (porta→`TarefaJpaRepository` + persistence mapper) em `.../service/repository/impl/TarefaRepositoryImpl.java` (faz T028 passar)
- [X] T030 Implementar adaptador `UsuarioRepositoryImpl` em `.../service/repository/impl/UsuarioRepositoryImpl.java`

### Segurança (OAuth2 Resource Server — Google)

- [X] T031 [P] Teste unitário do validador de audiência do JWT em `.../configuration/AudienceValidatorTest.java` (aceita `aud` correto; rejeita inválido)
- [X] T032 Implementar `SecurityConfiguration` + `AudienceValidator` (OAuth2 Resource Server, decoder do Google via issuer/JWKS, validação de assinatura+expiração+audiência, exige auth nas rotas de negócio) em `.../configuration/SecurityConfiguration.java` (faz T031 passar)

### Usuário autenticado (provisão a partir do token)

- [X] T033 [P] Teste unitário `UsuarioServiceTest` (resolve/provisiona pelas claims `sub`/`email`/`name`; idempotente por email) em `src/test/java/github/mlmatheus/todolist/service/UsuarioServiceTest.java`
- [X] T034 Criar interface `UsuarioService` (`.../service/UsuarioService.java`) e impl `.../service/impl/UsuarioServiceImpl.java` (depende da porta `UsuarioRepository`) (faz T033 passar)

### Tratamento de erros padronizado

- [X] T035 [P] Implementar DTO `ErroResponse` (timestamp, http_status, error_message, erros: map<campo,lista>) em `.../service/dto/response/ErroResponse.java`
- [X] T036 [P] Criar exceções de domínio `TarefaNaoEncontradaException` e `AcessoNegadoException` em `.../infrastructure/exception/`
- [X] T037 Teste unitário `GlobalExceptionHandlerTest` (validação→400, auth→401/403, não encontrado→404, fallback→500, formato do corpo) em `.../infrastructure/exception/handler/GlobalExceptionHandlerTest.java`
- [X] T038 Implementar `GlobalExceptionHandler` (`@RestControllerAdvice`) em `.../infrastructure/exception/handler/GlobalExceptionHandler.java` (faz T037 passar)

### Observabilidade (contexto de log por request)

- [X] T039 Implementar filtro que popula MDC com `request_id` e `id_usuario` por request em `.../infrastructure/RequestLoggingFilter.java`

### Documentação e contrato do controller (esqueleto)

- [X] T040 [P] Implementar `SwaggerConfiguration` (metadados OpenAPI + esquema de segurança bearer JWT) em `.../configuration/SwaggerConfiguration.java`
- [X] T041 Criar interface de contrato `ITodoListController` (assinaturas dos 4 endpoints + anotações springdoc, base path `/todo-list/v1`) em `.../controller/contract/ITodoListController.java`
- [X] T042 Criar `TodoListController` (implementa `ITodoListController`, injeta serviços; métodos ainda sem lógica de story) em `.../controller/TodoListController.java`

### Infra de teste de integração e autenticação

- [X] T043 Criar classe base de integração com Testcontainers MySQL + Flyway + JWT de teste em `src/integrationTest/java/github/mlmatheus/todolist/AbstractIntegrationTest.java`
- [X] T044 Teste de integração `AutenticacaoIT`: request sem `Authorization` → 401 (cobre FR-016) em `src/integrationTest/java/github/mlmatheus/todolist/AutenticacaoIT.java`

**Checkpoint**: Base pronta — domínio puro, persistência (ports/adapters), segurança (com 401), usuário, erros e contrato existem. User stories podem começar.

---

## Phase 3: User Story 1 - Cadastrar e visualizar tarefas (Priority: P1) 🎯 MVP

**Goal**: Usuário autenticado cadastra uma tarefa (status inicial PENDENTE) e a vê na sua lista paginada.

**Independent Test**: Autenticar, `POST /tarefas` com título válido → 201; `GET /tarefas` → a tarefa aparece com status PENDENTE; cadastro sem título → 400.

### Tests for User Story 1 (MANDATORY — TDD) ⚠️

- [X] T045 [P] [US1] Teste unitário `Tarefa.criar(...)` (status PENDENTE, default prioridade MÉDIA, timestamps, validação de título obrigatório/limite) em `.../service/model/TarefaCriacaoTest.java`
- [X] T046 [P] [US1] Teste unitário `TarefaMapperTest` (domínio↔DTO: request→domínio, domínio→response) em `.../service/mapper/TarefaMapperTest.java`
- [X] T047 [P] [US1] Teste unitário `TarefaService.criar`/`listar` (porta mockada; escopo por usuário) em `.../service/TarefaServiceCriarListarTest.java`
- [X] T048 [US1] Teste de integração `POST /tarefas` (201) e `GET /tarefas` (página com a tarefa); título vazio → 400 em `src/integrationTest/java/github/mlmatheus/todolist/CriarListarTarefaIT.java`

### Implementation for User Story 1

- [X] T049 [P] [US1] DTO `CriarTarefaRequest` (validações: titulo obrigatório/≤150, descricao ≤2000, prioridade 1..3 opcional) em `.../service/dto/request/CriarTarefaRequest.java`
- [X] T050 [P] [US1] DTOs `TarefaResponse` e `PageTarefaResponse` em `.../service/dto/response/`
- [X] T051 [US1] Implementar `Tarefa.criar(...)` no domínio (PENDENTE, default MÉDIA, timestamps) em `.../service/model/Tarefa.java` (faz T045 passar)
- [X] T052 [US1] Implementar `TarefaMapper` (MapStruct, domínio↔DTO) em `.../service/mapper/TarefaMapper.java` (faz T046 passar)
- [X] T053 [US1] Criar interface `TarefaService` e impl `criar` + `listar` (básico, paginado, via porta, escopo do usuário) em `.../service/TarefaService.java` e `.../service/impl/TarefaServiceImpl.java` (faz T047 passar)
- [X] T054 [US1] Implementar `POST /tarefas` (201) e `GET /tarefas` (página) no `TodoListController`, ligando ao serviço e ao usuário autenticado (faz T048 passar)

**Checkpoint**: MVP funcional — cadastrar e visualizar tarefas, isolado por usuário e testável de ponta a ponta.

---

## Phase 4: User Story 2 - Concluir / reabrir tarefa (Priority: P2)

**Goal**: Usuário marca uma tarefa como CONCLUIDA e pode reabri-la (PENDENTE), idempotente, via `status` no corpo do `PATCH`.

**Independent Test**: Cadastrar tarefa; `PATCH /tarefas/{id}` com `{"status":"CONCLUIDA"}` → 200 CONCLUIDA; repetir → continua CONCLUIDA; reabrir → PENDENTE; tarefa de outro usuário → 404.

### Tests for User Story 2 (MANDATORY — TDD) ⚠️

- [X] T055 [P] [US2] Teste unitário `Tarefa.concluir()`/`reabrir()` (transições + idempotência) em `.../service/model/TarefaTransicaoStatusTest.java`
- [X] T056 [P] [US2] Teste unitário `TarefaService.atualizarStatus` (escopo por usuário; 404 quando não é dono) em `.../service/TarefaServiceStatusTest.java`
- [X] T057 [US2] Teste de integração `PATCH /tarefas/{id}` com status (concluir/reabrir/idempotência; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/ConcluirTarefaIT.java`

### Implementation for User Story 2

- [X] T058 [US2] Implementar `Tarefa.concluir()` e `Tarefa.reabrir()` no domínio (idempotentes, atualizam `dataAtualizacao`) em `.../service/model/Tarefa.java` (faz T055 passar)
- [X] T059 [P] [US2] DTO `AtualizarTarefaRequest` com campo opcional `status` (e demais campos opcionais) em `.../service/dto/request/AtualizarTarefaRequest.java`
- [X] T060 [US2] Implementar `TarefaService.atualizarStatus` (carrega via porta, aplica transição) em `.../service/impl/TarefaServiceImpl.java` (faz T056 passar)
- [X] T061 [US2] Implementar `PATCH /tarefas/{id}` no `TodoListController` tratando `status` (200) (faz T057 passar)

**Checkpoint**: Ciclo de vida da tarefa completo (cadastrar → concluir/reabrir).

---

## Phase 5: User Story 3 - Editar tarefa (Priority: P2)

**Goal**: Usuário altera título, descrição, prioridade e data de vencimento de uma tarefa existente.

**Independent Test**: Cadastrar tarefa; `PATCH /tarefas/{id}` alterando título/prioridade → 200 com novos valores; título vazio → 400; tarefa de outro usuário → 404.

### Tests for User Story 3 (MANDATORY — TDD) ⚠️

- [X] T062 [P] [US3] Teste unitário `Tarefa.atualizarConteudo(...)` (aplica campos válidos, valida título, atualiza timestamp) em `.../service/model/TarefaAtualizarConteudoTest.java`
- [X] T063 [P] [US3] Teste unitário `TarefaService.atualizarConteudo` (parcial; escopo por usuário; 404 de outro dono) em `.../service/TarefaServiceEditarTest.java`
- [X] T064 [US3] Teste de integração `PATCH /tarefas/{id}` editando conteúdo (200; título vazio→400; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/EditarTarefaIT.java`

### Implementation for User Story 3

- [X] T065 [US3] Implementar `Tarefa.atualizarConteudo(titulo, descricao, prioridade, dataVencimento)` no domínio em `.../service/model/Tarefa.java` (faz T062 passar)
- [X] T066 [US3] Implementar `TarefaService.atualizarConteudo` (atualização parcial) em `.../service/impl/TarefaServiceImpl.java` (faz T063 passar)
- [X] T067 [US3] Estender o `PATCH /tarefas/{id}` no `TodoListController` para tratar campos de conteúdo junto do status (faz T064 passar)

**Checkpoint**: Edição de conteúdo + transição de status no mesmo endpoint PATCH.

---

## Phase 6: User Story 5 - Filtrar tarefas (Priority: P2)

**Goal**: Usuário filtra a lista por status, prioridade e data de vencimento (igualdade, combináveis), com paginação, via `GET /tarefas`.

**Independent Test**: Cadastrar tarefas variadas; filtrar por cada critério e combinados → apenas correspondentes; sem correspondência → página vazia.

### Tests for User Story 5 (MANDATORY — TDD) ⚠️

- [X] T068 [P] [US5] Teste unitário `TarefaSpecificationTest` (predicados sobre `TarefaEntity`: status, prioridade, data_vencimento por igualdade, combinados e sempre escopo `usuario_id`) em `.../service/repository/spec/TarefaSpecificationTest.java`
- [X] T069 [US5] Teste de integração `GET /tarefas` com filtros (cada filtro, combinados, página vazia, paginação/sort) em `src/integrationTest/java/github/mlmatheus/todolist/FiltrarTarefasIT.java`

### Implementation for User Story 5

- [X] T070 [P] [US5] DTO `TarefaFiltroRequest` (status, prioridade, data_vencimento) em `.../service/dto/request/TarefaFiltroRequest.java`
- [X] T071 [US5] Implementar `TarefaSpecification` com `CriteriaBuilder` (filtros opcionais por igualdade + escopo `usuario_id`) em `.../service/repository/spec/TarefaSpecification.java` (faz T068 passar)
- [X] T072 [US5] Estender a porta/adaptador `TarefaRepository.listar(filtro, usuarioId, pageable)` (aplica Specification) e o `TarefaService.listar` em `.../service/repository/impl/TarefaRepositoryImpl.java` e `.../service/impl/TarefaServiceImpl.java`
- [X] T073 [US5] Ligar os query params de filtro/paginação no `GET /tarefas` do `TodoListController` (faz T069 passar)

**Checkpoint**: Listagem com filtros combináveis e paginação.

---

## Phase 7: User Story 4 - Excluir tarefa (Priority: P3)

**Goal**: Usuário remove permanentemente uma tarefa sua.

**Independent Test**: Cadastrar tarefa; `DELETE /tarefas/{id}` → 204; some das listagens; tarefa de outro usuário → 404.

### Tests for User Story 4 (MANDATORY — TDD) ⚠️

- [X] T074 [P] [US4] Teste unitário `TarefaService.excluir` (escopo por usuário; 404 de outro dono) em `.../service/TarefaServiceExcluirTest.java`
- [X] T075 [US4] Teste de integração `DELETE /tarefas/{id}` (204; some da lista; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/ExcluirTarefaIT.java`

### Implementation for User Story 4

- [X] T076 [US4] Implementar `TarefaService.excluir` (valida propriedade via porta e remove) em `.../service/impl/TarefaServiceImpl.java` (faz T074 passar)
- [X] T077 [US4] Implementar `DELETE /tarefas/{id}` (204) no `TodoListController` (faz T075 passar)

**Checkpoint**: CRUD completo + concluir + filtrar.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Acabamento, documentação executável e verificação final de qualidade.

- [X] T078 [P] Criar arquivos `.http` (httpyac) de todas as rotas em `requests/tarefas.http` (criar, listar com filtros/paginação, concluir/reabrir, editar, excluir; variáveis base_url e Bearer token)
- [X] T079 [P] Revisar e completar anotações springdoc no `ITodoListController` (descrições claras de cada rota, parâmetros, respostas e erros)
- [X] T080 [P] Criar `TodoListService` (orquestração) — AVALIADO e considerado desnecessário (sem orquestração além do `TarefaService`); não criado para evitar artefato vazio (YAGNI)
- [X] T081 `mvn clean verify` (45 unitários + gate JaCoCo 100%) ✅ e `mvn verify -P integration` (17 testes Testcontainers) ✅ — ambos PASSAM
- [X] T082 [P] Fluxo de API validado ponta a ponta pelos testes de integração (Testcontainers MySQL); smoke manual via docker-compose opcional (ver quickstart)
- [X] T083 [P] Atualizar `README.md` com instruções de build, execução (compose + profile `mysql`), testes e documentação da API

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências.
- **Foundational (Phase 2)**: depende do Setup. BLOQUEIA todas as user stories.
- **User Stories (Phase 3–7)**: todas dependem da Foundational.
  - US1 (P1) primeiro (MVP).
  - US2, US3, US5 (P2) após a Foundational; US3 estende o endpoint PATCH introduzido em US2 (rodar US2 antes de US3).
  - US5 estende o `GET /tarefas` (US1) e a porta/adaptador `listar`.
  - US4 (P3) por último.
- **Polish (Phase 8)**: depois das user stories desejadas.

### Camadas (dentro da Foundational)

- Domínio puro (T013–T017) → entidades/JPA (T018–T023) → ports + mapper + adapters (T024–T030).
- Segurança (T031–T032) e usuário (T033–T034) antes dos testes que exigem autenticação.
- Contrato/controller (T040–T042) antes do teste de 401 (T044) e dos endpoints das stories.

### Acoplamentos entre stories

- **US3 → US2**: ambas usam `PATCH /tarefas/{id}`. US2 cria o endpoint (status); US3 o estende (conteúdo).
- **US5 → US1**: ambas usam `GET /tarefas` e a porta `listar`. US1 cria a listagem; US5 adiciona filtros via Specification.
- US4 é independente das demais (apenas Foundational).

### Within Each User Story

- Testes (unit + integração) escritos e falhando ANTES da implementação (TDD).
- Domínio (model) → serviço → endpoint.

### Parallel Opportunities

- Setup: T002–T008 em paralelo após T001.
- Foundational `[P]`: T009/T010, T013, T015/T016, T018/T019, T020/T021, T022/T023, T024/T025, T026, T031, T033, T035/T036, T040 (arquivos distintos).
- US1: T045/T046/T047 (testes) em paralelo; T049/T050 (DTOs) em paralelo.
- Por story, os testes marcados `[P]` rodam juntos antes da implementação.

---

## Parallel Example: User Story 1

```bash
# Testes da US1 (escrever primeiro, devem FALHAR):
Task: "T045 [US1] Teste unitário Tarefa.criar(...)"
Task: "T046 [US1] Teste unitário TarefaMapper (domínio↔DTO)"
Task: "T047 [US1] Teste unitário TarefaService.criar/listar"

# DTOs da US1 em paralelo:
Task: "T049 [US1] DTO CriarTarefaRequest"
Task: "T050 [US1] DTOs TarefaResponse/PageTarefaResponse"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1: Setup.
2. Phase 2: Foundational (CRÍTICO — bloqueia tudo).
3. Phase 3: US1 (cadastrar + visualizar).
4. **PARAR e VALIDAR**: testar US1 ponta a ponta (com 100% de cobertura).
5. Deploy/demo do MVP.

### Incremental Delivery

1. Setup + Foundational → base pronta.
2. US1 → testar → entregar (MVP).
3. US2 → concluir/reabrir → testar → entregar.
4. US3 → editar → testar → entregar.
5. US5 → filtrar → testar → entregar.
6. US4 → excluir → testar → entregar.
7. Polish.

### Notes

- TDD obrigatório: verifique que o teste falha antes de implementar.
- Domínio puro: nenhuma anotação de framework em `service/model`; persistência só nas entidades JPA e adaptadores.
- `mvn verify` falha se a cobertura ficar abaixo de 100% (linha e branch).
- Commit após cada tarefa ou grupo lógico.
- Cada user story deve permanecer independentemente testável.
