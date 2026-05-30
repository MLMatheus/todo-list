# Phase 1 — Data Model: Gerenciador de Tarefas

**Feature**: 001-task-manager | **Date**: 2026-05-30

Modelo derivado da spec (entidades Usuario, Tarefa) e do brief técnico (colunas e tipos).
Pacote de domínio: `github.mlmatheus.todolist.service.model`.

## Entidade: Usuario

Proprietário das tarefas, provisionado a partir do token Google.

| Campo | Tipo (domínio) | Coluna (MySQL) | Restrições |
|-------|----------------|----------------|------------|
| id | String (UUID) | `id CHAR(36)` | PK, NOT NULL |
| nome | String | `nome VARCHAR(255)` | NOT NULL |
| email | String | `email VARCHAR(320)` | NOT NULL, UNIQUE |

- **Relacionamento**: 1 Usuario → N Tarefa (`tarefa.usuario_id` referencia `usuario.id`).
- **Provisionamento**: criado/atualizado a partir das claims do JWT (`sub`→origem do id,
  `email`, `name`). `email` único garante idempotência do upsert.
- **Regras**: `nome` e `email` obrigatórios; `email` em formato válido.

## Entidade/Agregado: Tarefa

Raiz de agregado do bounded context. Concentra as regras de negócio (criação, edição,
transição de status).

| Campo | Tipo (domínio) | Coluna (MySQL) | Restrições |
|-------|----------------|----------------|------------|
| id | String (UUID) | `id CHAR(36)` | PK, NOT NULL |
| titulo | String | `titulo VARCHAR(150)` | NOT NULL, 1..150 chars, não em branco |
| descricao | String | `descricao VARCHAR(2000)` | NULL, ≤ 2000 chars |
| status | Status (enum) | `status VARCHAR(20)` | NOT NULL, default `PENDENTE` |
| prioridade | Prioridade (int VO) | `prioridade INT` | NOT NULL, ∈ {1,2,3} |
| data_vencimento | LocalDate | `data_vencimento DATE` | NULL |
| data_criacao | LocalDateTime | `data_criacao DATETIME(3)` | NOT NULL (set na criação) |
| data_atualizacao | LocalDateTime | `data_atualizacao DATETIME(3)` | NOT NULL (set na criação e em cada update) |
| usuario_id | String (FK) | `usuario_id CHAR(36)` | NOT NULL, FK → usuario(id) |

### Value Objects

- **Status** (enum): `PENDENTE`, `CONCLUIDA`.
  - Persistido como `@Enumerated(EnumType.STRING)`.
- **Prioridade** (VO sobre int): valores `1=BAIXA`, `2=MÉDIA`, `3=ALTA`.
  - Conversão int ↔ enum validada; valor fora de `1..3` → erro de validação.

### Regras de validação (origem: spec FR-003..FR-016)

- `titulo` obrigatório e não em branco (FR-004); tamanho limitado (FR-015).
- `descricao` opcional, tamanho limitado (FR-015).
- `prioridade` obrigatória na persistência; se ausente na criação, aplica-se default `2` (MÉDIA).
  *(Decisão de default — sinalizada; a spec marcava prioridade como opcional no cadastro.)*
- `status` inicial sempre `PENDENTE` (FR-005).
- Toda operação exige que `tarefa.usuario_id` == usuário autenticado (FR-002, FR-016);
  caso contrário → 404/403 (não vaza existência de tarefa de outro usuário).

### Comportamentos do agregado (regras de negócio no domínio — DDD)

- `Tarefa.criar(...)`: monta tarefa válida com `status=PENDENTE`, timestamps preenchidos.
- `concluir()`: `PENDENTE → CONCLUIDA`; se já `CONCLUIDA`, no-op (idempotente, FR-009).
- `reabrir()`: `CONCLUIDA → PENDENTE`; se já `PENDENTE`, no-op (idempotente, FR-009).
- `atualizarConteudo(titulo, descricao, prioridade, dataVencimento)`: aplica alterações válidas
  e atualiza `data_atualizacao`.

### Transições de estado

```text
        concluir()
PENDENTE ─────────────▶ CONCLUIDA
   ▲                        │
   └────────────────────────┘
          reabrir()

(concluir em CONCLUIDA = no-op; reabrir em PENDENTE = no-op)
```

## Índices e integridade (migrations Flyway)

- `usuario`: PK(`id`), UNIQUE(`email`).
- `tarefa`: PK(`id`), FK(`usuario_id`) → `usuario(id)`.
- Índices de apoio aos filtros: `idx_tarefa_usuario (usuario_id)`,
  `idx_tarefa_usuario_status (usuario_id, status)`,
  `idx_tarefa_usuario_venc (usuario_id, data_vencimento)`.
- **Rationale dos índices**: todo filtro é sempre escopado por `usuario_id`; os índices compostos
  cobrem os filtros mais comuns (status e data de vencimento) por usuário.

## Mapeamento DTO ↔ entidade (MapStruct — `TarefaMapper`)

- `CriarTarefaRequest` → `Tarefa` (status/timestamps definidos pelo domínio, não pelo request).
- `AtualizarTarefaRequest` (campos opcionais: titulo, descricao, prioridade, data_vencimento,
  status) → aplica em `Tarefa` via comportamentos do agregado.
- `Tarefa` → `TarefaResponse` (id, titulo, descricao, status, prioridade, data_vencimento).
