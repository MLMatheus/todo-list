# Phase 0 — Research: Gerenciador de Tarefas

**Feature**: 001-task-manager | **Date**: 2026-05-30

O brief técnico do usuário definiu a maioria das escolhas. Esta pesquisa consolida decisões,
resolve as lacunas spec ↔ contrato e registra as práticas adotadas. Não restam marcadores
`NEEDS CLARIFICATION`.

## D1 — Stack base (Java 21 + Spring Boot 3.5)

- **Decisão**: Java 21 (LTS) com Spring Boot 3.5, build Maven. Módulos: Spring Web, Spring
  Data JPA, Validation, Security OAuth2 Resource Server.
- **Rationale**: Especificado pelo usuário; stack madura para REST + JPA + segurança.
- **Alternativas**: Gradle (rejeitado — brief implica Maven via "maven profile integration").

## D2 — Persistência: MySQL + Flyway

- **Decisão**: MySQL 8.x; schema gerenciado por Flyway (`V1__create_usuario.sql`,
  `V2__create_tarefa.sql`). JPA/Hibernate com `ddl-auto: validate` (nunca `update`/`create`).
- **Rationale**: Migrations versionadas garantem reprodutibilidade e alinham schema ao
  data-model; `validate` impede divergência entre entidades e banco.
- **Alternativas**: `hibernate.ddl-auto=update` (rejeitado — não auditável, perigoso em produção).
- **IDs**: `String` (UUID em `CHAR(36)`), gerado pela aplicação. Determinístico nos testes via
  abstração de provedor de ID/relógio (testabilidade + 100% cobertura sem aleatoriedade real).

## D3 — Autenticação: Google como Identity Provider (OAuth2 Resource Server)

- **Decisão**: A API é um **Resource Server** que valida o JWT (ID token) do Google a cada
  request. Validações obrigatórias: **assinatura** (via JWKS `https://www.googleapis.com/oauth2/v3/certs`),
  **expiração** (`exp`) e **audiência** (`aud` == client id configurado). Issuer
  (`https://accounts.google.com`) também validado.
- **Implementação**: `spring-boot-starter-oauth2-resource-server` com
  `spring.security.oauth2.resourceserver.jwt.issuer-uri` (descobre JWKS) + um
  `OAuth2TokenValidator` adicional para `aud`. `SecurityConfiguration` exige autenticação em
  todas as rotas de negócio; libera Swagger/health conforme necessário.
- **Identidade do usuário**: claim `sub` (id estável do Google) + `email` + `name`. Um
  `UsuarioService` resolve/provisiona o `Usuario` local na primeira request (upsert idempotente).
- **Rationale**: Resource Server é o padrão Spring para validar tokens de terceiros sem manter
  credenciais; valida exatamente o que o brief pede.
- **Alternativas**: Validação manual do token com biblioteca do Google (rejeitado — reimplementa
  o que o starter já faz com cache de JWKS e rotação de chaves).

## D4 — Filtros dinâmicos: CriteriaBuilder + JPA Specification

- **Decisão**: `TarefaRepository extends JpaRepository<Tarefa,String>, JpaSpecificationExecutor<Tarefa>`.
  Um `TarefaSpecification` constrói predicados com `CriteriaBuilder` para `status`, `prioridade`,
  `data_vencimento` e sempre `usuario_id` = usuário autenticado. Paginação via `Pageable`.
- **Rationale**: "open specification" (Spring Data Specifications) compõe filtros opcionais de
  forma type-safe e testável, atendendo à exigência explícita do brief.
- **Alternativas**: Query Methods derivados (rejeitado — explode em combinações de filtros);
  QueryDSL (rejeitado — dependência extra desnecessária).

## D5 — Reconciliação: concluir/reabrir tarefa (FR-008) — **sinalizar ao usuário**

- **Lacuna**: os endpoints fornecidos (POST/GET/PATCH/DELETE) não incluem mudança de `status`;
  a spec exige concluir e reabrir.
- **Decisão**: o `PATCH /tarefas/{id}` aceita `status` como campo **opcional** no corpo. Quando
  presente, transiciona o agregado (`PENDENTE` ⇄ `CONCLUIDA`) via `tarefa.concluir()` /
  `tarefa.reabrir()`. Demais campos seguem como atualização parcial. Operação idempotente (FR-009).
- **Rationale**: Mantém a superfície de 4 endpoints e respeita a semântica de PATCH (atualização
  parcial). Evita um endpoint dedicado adicional.
- **Alternativa registrada**: sub-recurso `PATCH /tarefas/{id}/status`. Pode ser adotada se o
  usuário preferir separar a transição de estado da edição de conteúdo.

## D6 — Mapeamento de `prioridade` (int ↔ value object)

- **Decisão**: `prioridade` é inteiro validado em `1..3`, mapeado para `Prioridade`
  (`1=ALTA, 2=MÉDIA, 3=BAIXA` — convenção P1/P2/P3, menor número = maior prioridade).
  Persistido como `INT`. Default `2`=MÉDIA quando omitido. Valores fora do intervalo → erro de
  validação 400.
- **Rationale**: Concilia o `int` do brief com a semântica Baixa/Média/Alta da spec, mantendo um
  value object de domínio com significado. A ordenação P1/P2/P3 (menor = mais urgente) foi a
  escolhida pelo usuário no replanejamento.
- **Alternativa**: persistir o nome do enum como string (rejeitado — brief define `int`).

## D7 — Status no banco (string) ↔ domínio (enum)

- **Decisão**: coluna `status` `VARCHAR`, persistindo o nome do enum `Status` (`PENDENTE`,
  `CONCLUIDA`) via `@Enumerated(EnumType.STRING)`. Default na criação: `PENDENTE`.
- **Rationale**: legível no banco, estável a reordenações (diferente de `ORDINAL`).

## D8 — Documentação: springdoc-openapi

- **Decisão**: springdoc-openapi (Swagger UI). Toda anotação de documentação (operações,
  parâmetros, respostas, schemas, segurança) fica na **interface de contrato**
  `ITodoListController` e na `SwaggerConfiguration` (metadados gerais + esquema de segurança
  bearer JWT), conforme exigido.
- **Rationale**: Centraliza documentação no contrato, separando-a da implementação (ISP/SRP).

## D9 — Logs estruturados em JSON

- **Decisão**: Logback com `logstash-logback-encoder` (`logback-spring.xml`), emitindo JSON com
  campos `id_usuario`, `request_id`, `datetime` (`yyyy-MM-dd HH:mm:ss.SSS`), `log_level`,
  `message`, `instance_id`. `request_id` e `id_usuario` propagados via MDC por um filtro;
  `instance_id` resolvido na inicialização (ex.: hostname/env). Constantes em
  `infrastructure/constants/Log.java`.
- **Rationale**: JSON estruturado é parseável por agregadores; MDC injeta contexto por request
  sem poluir as mensagens.
- **Alternativas**: log textual (rejeitado — brief exige JSON estruturado).

## D10 — Tratamento de erros padronizado

- **Decisão**: `@RestControllerAdvice GlobalExceptionHandler` em `infrastructure/exception/handler`.
  Formato de erro:
  `{ timestamp("yyyy-MM-dd HH:mm:ss.SSS"), http_status:int, error_message:string, erros: { campo: [mensagens] } }`.
  Mapeia: validação (`MethodArgumentNotValidException` → 400), autenticação/autorização (401/403),
  não encontrado (404), e fallback erro interno (500). Exceções de domínio dedicadas
  (`TarefaNaoEncontradaException`, `AcessoNegadoException`).
- **Rationale**: respostas consistentes e testáveis; cobre os casos exigidos (validação, auth,
  erro interno).

## D11 — Testes: TDD, Testcontainers e cobertura 100%

- **Decisão**:
  - Unitários: JUnit 5 + Mockito (`mvn test`), cobrindo domínio, serviços, mappers, specifications,
    handler de erros e utilitários.
  - Integração: Testcontainers MySQL sob Maven profile `integration` (fonte `src/integrationTest`),
    exercitando controllers + JPA + Flyway + segurança (JWT mockado/decoder de teste) ponta a ponta.
  - Cobertura: JaCoCo com regra de verificação **100%** (linha e branch) no `verify`.
- **Exclusões de cobertura (explícitas e justificadas, conforme constituição)**:
  - `TodoListApplication` (bootstrap `main`): sem lógica testável.
  - Interfaces/contratos e DTOs/records sem lógica (somente dados).
  - Classes de configuração puramente declarativas, quando exercitadas indiretamente pelos
    testes de integração; caso contenham lógica, são cobertas.
- **Rationale**: TDD + 100% são NÃO NEGOCIÁVEIS na constituição; Testcontainers garante paridade
  com MySQL real em vez de banco em memória.
- **Alternativas**: H2 para integração (rejeitado — divergências de dialeto vs MySQL).

## D12 — Containerização e orquestração

- **Decisão**: `Dockerfile` multi-stage (stage build com Maven + JDK 21 → stage runtime com JRE
  slim, usuário não-root, JAR em camadas para cache). `docker-compose.yml` com serviços `app` e
  `mysql`. Profile/flag `mysql` do Compose permite subir **apenas** o banco
  (`docker compose --profile mysql up`) para rodar a app pela IDE contra o MySQL no host.
- **Rationale**: build otimizado para produção e fluxo de desenvolvimento local flexível,
  exatamente como pedido.

## D13 — Arquivos .http (httpyac)

- **Decisão**: pasta `requests/` com arquivos `.http` (httpyac) para todas as rotas
  (criar, listar com filtros/paginação, atualizar, concluir/reabrir, excluir), incluindo variáveis
  de ambiente para `base_url` e `Authorization: Bearer {{token}}`.
- **Rationale**: cliente HTTP padrão do projeto; mantém exemplos executáveis versionados.

## D14 — Separação domínio puro × persistência (decisão D1 do `/speckit-analyze`)

- **Decisão**: o modelo de domínio (`service/model`) é composto por POJOs puros, sem anotações
  de framework. A persistência usa entidades JPA separadas (`service/repository/entity`),
  acessadas por **portas** de domínio (`TarefaRepository`, `UsuarioRepository`) com
  **adaptadores** (`repository/impl`) e um *persistence mapper* (MapStruct). Specifications
  (`CriteriaBuilder`) atuam sobre as entidades JPA na camada de infraestrutura.
- **Rationale**: cumpre o Princípio I (DDD) da constituição sem ressalvas — o domínio fica
  independente de framework/banco. Resolve a issue **CRITICAL D1** apontada na análise.
- **Custo aceito**: camada extra de mapeamento domínio↔entidade (ports/adapters + mapper).

## Resumo de decisões (resolvidas)

| # | Tema | Decisão adotada | Origem |
|---|------|-----------------|--------|
| 1 | Concluir/reabrir | `status` opcional no corpo do PATCH | replan |
| 2 | Prioridade | int `1..3` → `1=ALTA, 2=MÉDIA, 3=BAIXA`; default 2 (MÉDIA) | replan |
| 3 | Status | enum {PENDENTE, CONCLUIDA} | replan |
| 4 | Arquitetura | Domínio puro separado das entidades JPA (ports/adapters + mapper) | analyze D1 |
| 5 | Filtro de data | Igualdade por data única; intervalo fora de escopo | analyze F1 |
| 6 | Default de prioridade | Registrado na spec (FR-010): MÉDIA quando omitido | analyze U1 |
| 7 | Teste de 401 | Task de integração para request sem token | analyze E1 |
