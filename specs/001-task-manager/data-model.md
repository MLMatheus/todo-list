# Phase 1 — Data Model: Gerenciador de Tarefas

**Feature**: 001-task-manager | **Date**: 2026-05-30

Modelo derivado da spec (entidades Usuario, Tarefa) e do brief técnico. **Decisão D1** do
replanejamento: o **domínio é puro** (sem framework) e fica separado das **entidades de
persistência** (JPA). Há duas representações ligadas por um *persistence mapper*.

- Domínio puro: `github.mlmatheus.todolist.service.model`
- Persistência (JPA): `github.mlmatheus.todolist.service.repository.entity`
- Portas + adaptadores: `github.mlmatheus.todolist.service.repository` (+ `impl`, `jpa`, `spec`)

---

## 1. Modelo de Domínio (puro — `service/model`)

Sem anotações de framework. Concentra as regras de negócio.

### Usuario (entidade de domínio)

| Campo | Tipo | Restrições |
|-------|------|------------|
| id | String (UUID) | obrigatório |
| nome | String | obrigatório |
| email | String | obrigatório, formato válido, único no sistema |

- 1 Usuario → N Tarefa. Provisionado a partir das claims do JWT do Google (`sub`/`email`/`name`).

### Tarefa (raiz de agregado)

| Campo | Tipo | Restrições |
|-------|------|------------|
| id | String (UUID) | obrigatório |
| titulo | String | obrigatório, não em branco, 1..150 |
| descricao | String | opcional, ≤ 2000 |
| status | Status (VO) | obrigatório, default `PENDENTE` |
| prioridade | Prioridade (VO) | obrigatório; default `MÉDIA` se omitido na criação |
| dataVencimento | LocalDate | opcional |
| dataCriacao | LocalDateTime | definido na criação |
| dataAtualizacao | LocalDateTime | definido na criação e em cada alteração |
| usuarioId | String | obrigatório (referência ao proprietário) |

### Value Objects

- **Status** (enum): `PENDENTE`, `CONCLUIDA`.
- **Prioridade** (VO sobre int): `1=ALTA`, `2=MÉDIA`, `3=BAIXA` (menor número = maior
  prioridade). Conversão int↔enum validada; valor fora de `1..3` → inválido.

### Comportamentos do agregado (regras de negócio no domínio)

- `Tarefa.criar(...)`: monta tarefa válida com `status=PENDENTE`, `prioridade` default MÉDIA
  quando ausente, timestamps preenchidos.
- `concluir()`: `PENDENTE → CONCLUIDA`; se já `CONCLUIDA`, no-op (idempotente, FR-009).
- `reabrir()`: `CONCLUIDA → PENDENTE`; se já `PENDENTE`, no-op (idempotente, FR-009).
- `atualizarConteudo(titulo, descricao, prioridade, dataVencimento)`: aplica alterações válidas
  e atualiza `dataAtualizacao`.

### Regras de validação (origem: spec FR-003..FR-016)

- `titulo` obrigatório e não em branco (FR-004); tamanho limitado a 150 (FR-015).
- `descricao` opcional, ≤ 2000 (FR-015).
- `prioridade` default `MÉDIA` (=2) quando ausente no cadastro (FR-010); valores válidos 1..3.
- `status` inicial sempre `PENDENTE` (FR-005).
- Toda operação exige `tarefa.usuarioId` == usuário autenticado (FR-002, FR-016); caso
  contrário → 404 (não vaza existência de tarefa de outro usuário).

### Transições de estado

```text
        concluir()
PENDENTE ─────────────▶ CONCLUIDA
   ▲                        │
   └────────────────────────┘
          reabrir()

(concluir em CONCLUIDA = no-op; reabrir em PENDENTE = no-op)
```

---

## 2. Modelo de Persistência (JPA — `service/repository/entity`)

Entidades anotadas, espelhando o domínio. **Nenhuma regra de negócio aqui** — apenas mapeamento.

### UsuarioEntity → tabela `usuario`

| Campo | Coluna (MySQL) | Restrições |
|-------|----------------|------------|
| id | `id VARCHAR(36)` | PK, NOT NULL |
| nome | `nome VARCHAR(255)` | NOT NULL |
| email | `email VARCHAR(320)` | NOT NULL, UNIQUE |

### TarefaEntity → tabela `tarefa`

| Campo | Coluna (MySQL) | Restrições |
|-------|----------------|------------|
| id | `id VARCHAR(36)` | PK, NOT NULL |
| titulo | `titulo VARCHAR(150)` | NOT NULL |
| descricao | `descricao VARCHAR(2000)` | NULL |
| status | `status VARCHAR(20)` | NOT NULL (`@Enumerated(STRING)`) |
| prioridade | `prioridade INT` | NOT NULL, ∈ {1,2,3} |
| dataVencimento | `data_vencimento DATE` | NULL |
| dataCriacao | `data_criacao DATETIME(3)` | NOT NULL |
| dataAtualizacao | `data_atualizacao DATETIME(3)` | NOT NULL |
| usuarioId | `usuario_id VARCHAR(36)` | NOT NULL, FK → usuario(id) |

### Índices e integridade (migrations Flyway)

- `usuario`: PK(`id`), UNIQUE(`email`).
- `tarefa`: PK(`id`), FK(`usuario_id`) → `usuario(id)`.
- Índices de apoio aos filtros: `idx_tarefa_usuario (usuario_id)`,
  `idx_tarefa_usuario_status (usuario_id, status)`,
  `idx_tarefa_usuario_venc (usuario_id, data_vencimento)`.
- **Rationale**: todo filtro é sempre escopado por `usuario_id`; os índices compostos cobrem os
  filtros mais comuns (status e data de vencimento) por usuário.

---

## 3. Portas, adaptadores e mapeamentos

- **Portas** (`service/repository/TarefaRepository`, `UsuarioRepository`): interfaces expressas
  em termos do **domínio** (recebem/retornam `Tarefa`/`Usuario`). Os serviços dependem delas.
- **Spring Data** (`service/repository/jpa/*JpaRepository`): `JpaRepository<*Entity,String>`;
  `TarefaJpaRepository` também `JpaSpecificationExecutor<TarefaEntity>`.
- **Adaptadores** (`service/repository/impl/*RepositoryImpl`): implementam as portas usando o
  Spring Data + o persistence mapper para traduzir entidade↔domínio.
- **Specification** (`service/repository/spec/TarefaSpecification`): predicados `CriteriaBuilder`
  sobre `TarefaEntity` para filtros (status, prioridade, **data_vencimento por igualdade**) e
  sempre escopo `usuario_id`. Vive na camada de infraestrutura (não no domínio).

### Mapeamentos (MapStruct)

- `TarefaPersistenceMapper`: `Tarefa` (domínio) ↔ `TarefaEntity` (JPA); idem `Usuario`.
- `TarefaMapper`: `CriarTarefaRequest`/`AtualizarTarefaRequest` → domínio; `Tarefa` → `TarefaResponse`.
  Status/timestamps são responsabilidade do domínio, nunca do request.
