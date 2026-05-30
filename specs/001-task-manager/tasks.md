---

description: "Task list for Gerenciador de Tarefas (001-task-manager)"
---

# Tasks: Gerenciador de Tarefas

**Input**: Design documents from `/specs/001-task-manager/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/openapi.yaml

**Tests**: OBRIGATÓRIOS. A constituição do projeto exige TDD (NÃO NEGOCIÁVEL) e cobertura 100%.
Cada tarefa de implementação é precedida por um teste que falha (Red → Green → Refactor).

**Organization**: Tarefas agrupadas por user story, em ordem de prioridade (P1 → P2 → P3).

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

- [ ] T001 Criar `pom.xml` (Maven) com Java 21 e Spring Boot 3.5 (Web, Data JPA, Validation, OAuth2 Resource Server), Flyway, MySQL Connector/J, springdoc-openapi, logstash-logback-encoder, MapStruct; dependências de teste JUnit 5, Mockito, Testcontainers (mysql)
- [ ] T002 [P] Criar `src/main/java/github/mlmatheus/todolist/TodoListApplication.java` (classe de bootstrap Spring Boot)
- [ ] T003 [P] Criar `src/main/resources/application.yml` (datasource MySQL, `jpa.hibernate.ddl-auto=validate`, Flyway, `oauth2.resourceserver.jwt.issuer-uri`, `app.security.google.audience`, `app.instance-id`, base-path `/todo-list/v1`)
- [ ] T004 [P] Configurar JaCoCo no `pom.xml` com regra de cobertura 100% (linha e branch) no goal `verify`, com exclusões explícitas justificadas (`TodoListApplication`, DTOs/records sem lógica, interfaces de contrato)
- [ ] T005 [P] Configurar Maven profile `integration` no `pom.xml` (source set `src/integrationTest/java`, failsafe plugin, ativação do Testcontainers)
- [ ] T006 [P] Criar `Dockerfile` multi-stage (build Maven+JDK 21 → runtime JRE slim, usuário não-root, JAR em camadas) otimizado para produção
- [ ] T007 [P] Criar `docker-compose.yml` com serviços `app` e `mysql`, incluindo profile `mysql` para subir apenas o banco
- [ ] T008 [P] Criar `src/main/resources/logback-spring.xml` com appender JSON (logstash-encoder) e campos `id_usuario`, `request_id`, `datetime` (`yyyy-MM-dd HH:mm:ss.SSS`), `log_level`, `message`, `instance_id`

**Checkpoint**: Projeto compila (`mvn compile`), sobe vazio e o pipeline de cobertura está armado.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Núcleo compartilhado que DEVE existir antes de qualquer user story (persistência base, segurança, usuário autenticado, erros, observabilidade, contrato do controller).

**⚠️ CRITICAL**: Nenhuma user story pode começar antes desta fase.

### Constantes e utilitários

- [ ] T009 [P] Criar `src/main/java/github/mlmatheus/todolist/infrastructure/constants/Geral.java` (base_url, constantes gerais)
- [ ] T010 [P] Criar `src/main/java/github/mlmatheus/todolist/infrastructure/constants/Log.java` (chaves JSON de log)
- [ ] T011 [P] Teste unitário de `DateFormatterUtils` em `src/test/java/github/mlmatheus/todolist/infrastructure/util/DateFormatterUtilsTest.java` (formato `yyyy-MM-dd HH:mm:ss.SSS`)
- [ ] T012 [P] Implementar `src/main/java/github/mlmatheus/todolist/infrastructure/util/DateFormatterUtils.java` (faz T011 passar)

### Modelo de domínio (base) e value objects

- [ ] T013 [P] Teste unitário de `Prioridade` (VO) em `.../service/model/PrioridadeTest.java` (mapeamento int↔enum `1=ALTA,2=MÉDIA,3=BAIXA`; valor fora de 1..3 inválido)
- [ ] T014 [P] Implementar enum/VO `Prioridade` em `src/main/java/github/mlmatheus/todolist/service/model/Prioridade.java` (faz T013 passar)
- [ ] T015 [P] Implementar enum `Status` em `src/main/java/github/mlmatheus/todolist/service/model/Status.java` (`PENDENTE`, `CONCLUIDA`)
- [ ] T016 [P] Implementar entidade `Usuario` em `src/main/java/github/mlmatheus/todolist/service/model/Usuario.java` (id, nome, email; mapeamento JPA)
- [ ] T017 Implementar entidade/agregado `Tarefa` (campos + mapeamento JPA, sem comportamentos de transição ainda) em `src/main/java/github/mlmatheus/todolist/service/model/Tarefa.java`

### Migrations (Flyway)

- [ ] T018 [P] Criar migration `src/main/resources/db/migration/V1__create_usuario.sql` (PK id CHAR(36), nome, email UNIQUE)
- [ ] T019 [P] Criar migration `src/main/resources/db/migration/V2__create_tarefa.sql` (colunas conforme data-model + FK usuario_id + índices `idx_tarefa_usuario`, `idx_tarefa_usuario_status`, `idx_tarefa_usuario_venc`)

### Repositórios

- [ ] T020 [P] Criar `TarefaRepository` (`JpaRepository<Tarefa,String>` + `JpaSpecificationExecutor<Tarefa>`) em `src/main/java/github/mlmatheus/todolist/service/repository/TarefaRepository.java`
- [ ] T021 [P] Criar `UsuarioRepository` (`JpaRepository<Usuario,String>`, busca por email) em `src/main/java/github/mlmatheus/todolist/service/repository/UsuarioRepository.java`

### Segurança (OAuth2 Resource Server — Google)

- [ ] T022 Teste unitário do validador de audiência do JWT em `.../configuration/AudienceValidatorTest.java` (aceita `aud` correto; rejeita `aud` inválido/expirado)
- [ ] T023 Implementar `SecurityConfiguration` (OAuth2 Resource Server, decoder do Google via issuer/JWKS, validação de assinatura+expiração+audiência, exige auth em todas as rotas de negócio) em `src/main/java/github/mlmatheus/todolist/configuration/SecurityConfiguration.java` (faz T022 passar)

### Usuário autenticado (provisão a partir do token)

- [ ] T024 Teste unitário de `UsuarioService` (resolve/provisiona usuário pelas claims `sub`/`email`/`name`; idempotente por email) em `src/test/java/github/mlmatheus/todolist/service/UsuarioServiceTest.java`
- [ ] T025 Criar interface `UsuarioService` em `.../service/UsuarioService.java` e implementação em `.../service/impl/UsuarioServiceImpl.java` (faz T024 passar)

### Tratamento de erros padronizado

- [ ] T026 [P] Implementar DTO `ErroResponse` (timestamp, http_status, error_message, erros: map<campo,lista>) em `.../service/dto/response/ErroResponse.java`
- [ ] T027 [P] Criar exceções de domínio `TarefaNaoEncontradaException` e `AcessoNegadoException` em `.../infrastructure/exception/`
- [ ] T028 Teste unitário de `GlobalExceptionHandler` (validação→400, auth→401/403, não encontrado→404, fallback→500, formato do corpo) em `.../infrastructure/exception/handler/GlobalExceptionHandlerTest.java`
- [ ] T029 Implementar `GlobalExceptionHandler` (`@RestControllerAdvice`) em `src/main/java/github/mlmatheus/todolist/infrastructure/exception/handler/GlobalExceptionHandler.java` (faz T028 passar)

### Observabilidade (contexto de log por request)

- [ ] T030 Implementar filtro de logging que popula MDC com `request_id` e `id_usuario` por request em `.../infrastructure/` (ex.: `RequestLoggingFilter.java`)

### Documentação e contrato do controller (esqueleto)

- [ ] T031 [P] Implementar `SwaggerConfiguration` (metadados OpenAPI + esquema de segurança bearer JWT) em `src/main/java/github/mlmatheus/todolist/configuration/SwaggerConfiguration.java`
- [ ] T032 Criar interface de contrato `ITodoListController` (assinaturas dos 4 endpoints + anotações springdoc, base path `/todo-list/v1`) em `src/main/java/github/mlmatheus/todolist/controller/contract/ITodoListController.java`
- [ ] T033 Criar `TodoListController` (implementa `ITodoListController`, injeta serviços; métodos ainda sem lógica de story) em `src/main/java/github/mlmatheus/todolist/controller/TodoListController.java`

### Infra de teste de integração

- [ ] T034 Criar classe base de integração com Testcontainers MySQL + Flyway + JWT de teste em `src/integrationTest/java/github/mlmatheus/todolist/AbstractIntegrationTest.java`

**Checkpoint**: Base pronta — segurança, persistência, usuário, erros e contrato existem. User stories podem começar.

---

## Phase 3: User Story 1 - Cadastrar e visualizar tarefas (Priority: P1) 🎯 MVP

**Goal**: Usuário autenticado cadastra uma tarefa (status inicial PENDENTE) e a vê na sua lista paginada.

**Independent Test**: Autenticar, `POST /tarefas` com título válido → 201; `GET /tarefas` → a tarefa aparece com status PENDENTE; cadastro sem título → 400.

### Tests for User Story 1 (MANDATORY — TDD) ⚠️

- [ ] T035 [P] [US1] Teste unitário `Tarefa.criar(...)` (status PENDENTE, timestamps, validação de título obrigatório/limite) em `.../service/model/TarefaCriacaoTest.java`
- [ ] T036 [P] [US1] Teste unitário de `TarefaMapper` (request→entity, entity→response) em `.../service/mapper/TarefaMapperTest.java`
- [ ] T037 [P] [US1] Teste unitário de `TarefaService.criar` e `listar` (básico, escopo por usuário) em `.../service/TarefaServiceCriarListarTest.java`
- [ ] T038 [US1] Teste de integração `POST /tarefas` (201 + corpo) e `GET /tarefas` (página com a tarefa); título vazio → 400 em `src/integrationTest/java/github/mlmatheus/todolist/CriarListarTarefaIT.java`

### Implementation for User Story 1

- [ ] T039 [P] [US1] DTO `CriarTarefaRequest` (validações: titulo obrigatório/≤150, descricao ≤2000, prioridade 1..3 opcional) em `.../service/dto/request/CriarTarefaRequest.java`
- [ ] T040 [P] [US1] DTOs `TarefaResponse` e `PageTarefaResponse` em `.../service/dto/response/`
- [ ] T041 [US1] Implementar `Tarefa.criar(...)` (factory de domínio: PENDENTE, default prioridade MÉDIA, timestamps) em `.../service/model/Tarefa.java` (faz T035 passar)
- [ ] T042 [US1] Implementar `TarefaMapper` (MapStruct) em `.../service/mapper/TarefaMapper.java` (faz T036 passar)
- [ ] T043 [US1] Criar interface `TarefaService` e implementar `criar` + `listar` (básico, paginado, escopo do usuário autenticado) em `.../service/TarefaService.java` e `.../service/impl/TarefaServiceImpl.java` (faz T037 passar)
- [ ] T044 [US1] Implementar no `TodoListController` o `POST /tarefas` (201) e o `GET /tarefas` (página) ligando ao serviço e ao usuário autenticado (faz T038 passar)

**Checkpoint**: MVP funcional — cadastrar e visualizar tarefas, isolado por usuário e testável de ponta a ponta.

---

## Phase 4: User Story 2 - Concluir / reabrir tarefa (Priority: P2)

**Goal**: Usuário marca uma tarefa como CONCLUIDA e pode reabri-la (PENDENTE), de forma idempotente, via `status` no corpo do `PATCH`.

**Independent Test**: Cadastrar tarefa; `PATCH /tarefas/{id}` com `{"status":"CONCLUIDA"}` → 200 e status CONCLUIDA; repetir → continua CONCLUIDA; reabrir → PENDENTE; tarefa de outro usuário → 404.

### Tests for User Story 2 (MANDATORY — TDD) ⚠️

- [ ] T045 [P] [US2] Teste unitário `Tarefa.concluir()`/`reabrir()` (transições e idempotência) em `.../service/model/TarefaTransicaoStatusTest.java`
- [ ] T046 [P] [US2] Teste unitário de `TarefaService.atualizarStatus` (escopo por usuário; 404 quando não é dono) em `.../service/TarefaServiceStatusTest.java`
- [ ] T047 [US2] Teste de integração `PATCH /tarefas/{id}` com status (concluir/reabrir/idempotência; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/ConcluirTarefaIT.java`

### Implementation for User Story 2

- [ ] T048 [US2] Implementar `Tarefa.concluir()` e `Tarefa.reabrir()` (idempotentes, atualizam `data_atualizacao`) em `.../service/model/Tarefa.java` (faz T045 passar)
- [ ] T049 [P] [US2] DTO `AtualizarTarefaRequest` com campo opcional `status` (e demais campos opcionais) em `.../service/dto/request/AtualizarTarefaRequest.java`
- [ ] T050 [US2] Implementar `TarefaService.atualizarStatus` (carrega tarefa do dono, aplica transição) em `.../service/impl/TarefaServiceImpl.java` (faz T046 passar)
- [ ] T051 [US2] Implementar `PATCH /tarefas/{id}` no `TodoListController` tratando `status` (200) (faz T047 passar)

**Checkpoint**: Ciclo de vida da tarefa completo (cadastrar → concluir/reabrir).

---

## Phase 5: User Story 3 - Editar tarefa (Priority: P2)

**Goal**: Usuário altera título, descrição, prioridade e data de vencimento de uma tarefa existente.

**Independent Test**: Cadastrar tarefa; `PATCH /tarefas/{id}` alterando título/prioridade → 200 com novos valores; título vazio → 400; tarefa de outro usuário → 404.

### Tests for User Story 3 (MANDATORY — TDD) ⚠️

- [ ] T052 [P] [US3] Teste unitário `Tarefa.atualizarConteudo(...)` (aplica campos válidos, valida título, atualiza timestamp) em `.../service/model/TarefaAtualizarConteudoTest.java`
- [ ] T053 [P] [US3] Teste unitário de `TarefaService.atualizarConteudo` (parcial; escopo por usuário; 404 de outro dono) em `.../service/TarefaServiceEditarTest.java`
- [ ] T054 [US3] Teste de integração `PATCH /tarefas/{id}` editando conteúdo (200; título vazio→400; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/EditarTarefaIT.java`

### Implementation for User Story 3

- [ ] T055 [US3] Implementar `Tarefa.atualizarConteudo(titulo, descricao, prioridade, dataVencimento)` em `.../service/model/Tarefa.java` (faz T052 passar)
- [ ] T056 [US3] Implementar `TarefaService.atualizarConteudo` (atualização parcial) em `.../service/impl/TarefaServiceImpl.java` (faz T053 passar)
- [ ] T057 [US3] Estender o `PATCH /tarefas/{id}` no `TodoListController` para tratar campos de conteúdo junto do status (faz T054 passar)

**Checkpoint**: Edição de conteúdo + transição de status no mesmo endpoint PATCH.

---

## Phase 6: User Story 5 - Filtrar tarefas (Priority: P2)

**Goal**: Usuário filtra a lista por status, prioridade e data de vencimento (combináveis), com paginação, via `GET /tarefas`.

**Independent Test**: Cadastrar tarefas variadas; filtrar por cada critério e combinados → apenas correspondentes; sem correspondência → página vazia.

### Tests for User Story 5 (MANDATORY — TDD) ⚠️

- [ ] T058 [P] [US5] Teste unitário de `TarefaSpecification` (predicados por status, prioridade, data_vencimento, combinados e sempre escopado por usuário) em `.../service/repository/TarefaSpecificationTest.java`
- [ ] T059 [US5] Teste de integração `GET /tarefas` com filtros (cada filtro, combinados, página vazia, paginação/sort) em `src/integrationTest/java/github/mlmatheus/todolist/FiltrarTarefasIT.java`

### Implementation for User Story 5

- [ ] T060 [P] [US5] DTO `TarefaFiltroRequest` (status, prioridade, data_vencimento) em `.../service/dto/request/TarefaFiltroRequest.java`
- [ ] T061 [US5] Implementar `TarefaSpecification` com `CriteriaBuilder` (filtros opcionais + escopo `usuario_id`) em `.../service/repository/TarefaSpecification.java` (faz T058 passar)
- [ ] T062 [US5] Estender `TarefaService.listar` para aplicar a Specification + `Pageable` em `.../service/impl/TarefaServiceImpl.java`
- [ ] T063 [US5] Ligar os query params de filtro/paginação no `GET /tarefas` do `TodoListController` (faz T059 passar)

**Checkpoint**: Listagem com filtros combináveis e paginação.

---

## Phase 7: User Story 4 - Excluir tarefa (Priority: P3)

**Goal**: Usuário remove permanentemente uma tarefa sua.

**Independent Test**: Cadastrar tarefa; `DELETE /tarefas/{id}` → 204; some das listagens; tarefa de outro usuário → 404.

### Tests for User Story 4 (MANDATORY — TDD) ⚠️

- [ ] T064 [P] [US4] Teste unitário de `TarefaService.excluir` (escopo por usuário; 404 de outro dono) em `.../service/TarefaServiceExcluirTest.java`
- [ ] T065 [US4] Teste de integração `DELETE /tarefas/{id}` (204; some da lista; 404 de outro usuário) em `src/integrationTest/java/github/mlmatheus/todolist/ExcluirTarefaIT.java`

### Implementation for User Story 4

- [ ] T066 [US4] Implementar `TarefaService.excluir` (valida propriedade e remove) em `.../service/impl/TarefaServiceImpl.java` (faz T064 passar)
- [ ] T067 [US4] Implementar `DELETE /tarefas/{id}` (204) no `TodoListController` (faz T065 passar)

**Checkpoint**: CRUD completo + concluir + filtrar.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Acabamento, documentação executável e verificação final de qualidade.

- [ ] T068 [P] Criar arquivos `.http` (httpyac) de todas as rotas em `requests/tarefas.http` (criar, listar com filtros/paginação, concluir/reabrir, editar, excluir; variáveis base_url e Bearer token)
- [ ] T069 [P] Revisar e completar anotações springdoc no `ITodoListController` (descrições claras de cada rota, parâmetros, respostas e erros)
- [ ] T070 [P] Criar `TodoListService` (orquestração) em `.../service/TodoListService.java` se necessário para compor operações, com teste unitário correspondente
- [ ] T071 Executar `mvn clean verify` e `mvn verify -P integration`; garantir 100% de cobertura (JaCoCo) e ajustar exclusões justificadas
- [ ] T072 [P] Validar o fluxo de smoke do `quickstart.md` (subir via docker-compose, executar os `.http`)
- [ ] T073 [P] Atualizar `README.md` com instruções de build, execução (compose + profile `mysql`), testes e documentação da API

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências.
- **Foundational (Phase 2)**: depende do Setup. BLOQUEIA todas as user stories.
- **User Stories (Phase 3–7)**: todas dependem da Foundational.
  - US1 (P1) primeiro (MVP).
  - US2, US3, US5 (P2) após a Foundational; US3 estende o endpoint PATCH introduzido em US2 (rodar US2 antes de US3).
  - US5 estende o `GET /tarefas` introduzido em US1.
  - US4 (P3) por último.
- **Polish (Phase 8)**: depois das user stories desejadas.

### Acoplamentos entre stories

- **US3 → US2**: ambas usam `PATCH /tarefas/{id}`. US2 cria o endpoint (status); US3 o estende (conteúdo).
- **US5 → US1**: ambas usam `GET /tarefas`. US1 cria a listagem; US5 adiciona filtros via Specification.
- US4 é independente das demais (apenas Foundational).

### Within Each User Story

- Testes (unit + integração) escritos e falhando ANTES da implementação (TDD).
- Domínio (model) → serviço → endpoint.

### Parallel Opportunities

- Setup: T002–T008 em paralelo após T001.
- Foundational: T009/T010, T011, T013, T018/T019, T020/T021, T026/T027, T031 são `[P]` (arquivos distintos).
- US1: T035/T036/T037 (testes) em paralelo; T039/T040 (DTOs) em paralelo.
- Por story, os testes marcados `[P]` rodam juntos antes da implementação.

---

## Parallel Example: User Story 1

```bash
# Testes da US1 (escrever primeiro, devem FALHAR):
Task: "T035 [US1] Teste unitário Tarefa.criar(...)"
Task: "T036 [US1] Teste unitário TarefaMapper"
Task: "T037 [US1] Teste unitário TarefaService.criar/listar"

# DTOs da US1 em paralelo:
Task: "T039 [US1] DTO CriarTarefaRequest"
Task: "T040 [US1] DTOs TarefaResponse/PageTarefaResponse"
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
- `mvn verify` falha se a cobertura ficar abaixo de 100% (linha e branch).
- Commit após cada tarefa ou grupo lógico.
- Cada user story deve permanecer independentemente testável.
