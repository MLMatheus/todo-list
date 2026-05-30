<!--
SYNC IMPACT REPORT
==================
Version change: TEMPLATE (não versionado) → 1.0.0
Rationale: Ratificação inicial — primeira definição concreta da constituição,
substituindo todos os placeholders do template. Bump = 1.0.0 (MAJOR inicial).

Princípios definidos:
  - I. Domain-Driven Design (DDD)
  - II. Test-Driven Development (TDD) — NÃO NEGOCIÁVEL
  - III. Cobertura de Testes 100% — NÃO NEGOCIÁVEL
  - IV. Princípios SOLID

Seções adicionadas:
  - Restrições de Arquitetura e Qualidade
  - Fluxo de Desenvolvimento e Portões de Qualidade
  - Governança

Seções removidas:
  - 5º slot de princípio do template (usamos 4 princípios, conforme entrada do usuário)

Status de propagação nos templates:
  ✅ .specify/templates/plan-template.md — seção "Constitution Check" é genérica;
     os portões passam a derivar destes 4 princípios. Sem edição estrutural necessária.
  ⚠ .specify/templates/tasks-template.md — declara "Tests are OPTIONAL"; isto conflita
     com TDD/cobertura 100% obrigatórios. Tarefas de teste DEVEM ser tratadas como
     obrigatórias quando gerarem tasks deste projeto. Acompanhar na geração de tasks.
  ✅ .specify/templates/spec-template.md — sem dependência direta de princípios.

Follow-ups / TODOs adiados: nenhum. Datas de ratificação e emenda = 2026-05-30.
-->

# Todo Constitution

## Princípios Fundamentais

### I. Domain-Driven Design (DDD)

A aplicação **DEVE** ser modelada a partir do domínio de negócio, não da infraestrutura.

- A lógica de negócio reside na camada de domínio e é independente de frameworks,
  banco de dados, UI ou bibliotecas externas.
- Entidades, Objetos de Valor (Value Objects), Agregados e Serviços de Domínio
  **DEVEM** ser expressos com uma Linguagem Ubíqua compartilhada entre código e negócio.
- Limites de contexto (Bounded Contexts) **DEVEM** ser explícitos; dependências cruzam
  esses limites apenas por contratos bem definidos (interfaces/portas).
- Detalhes de infraestrutura (persistência, rede, I/O) **DEVEM** ser acessados via
  abstrações (repositórios/portas), nunca referenciados diretamente pelo domínio.

**Racional**: Isolar o domínio mantém as regras de negócio testáveis, expressivas e
resistentes a mudanças tecnológicas, sustentando os demais princípios.

### II. Test-Driven Development (TDD) — NÃO NEGOCIÁVEL

Todo código de produção **DEVE** ser escrito em resposta a um teste que falha primeiro.

- O ciclo **Red-Green-Refactor** é obrigatório: escrever o teste → vê-lo falhar (Red)
  → implementar o mínimo para passar (Green) → refatorar com testes verdes (Refactor).
- É **PROIBIDO** escrever lógica de produção sem um teste prévio que a exija.
- Cada comportamento novo ou alterado começa por um teste; correções de bug começam
  por um teste que reproduz o defeito.
- Refatorações **DEVEM** manter a suíte verde do início ao fim.

**Racional**: O teste como guia força design simples, contratos claros e feedback
imediato, prevenindo regressões e código não exercitado.

### III. Cobertura de Testes 100% — NÃO NEGOCIÁVEL

A aplicação **DEVE** manter 100% de cobertura de testes.

- Cobertura de linhas e de branches **DEVE** ser 100%; o portão de CI/qualidade
  **DEVE** falhar abaixo desse limite.
- Exclusões de cobertura **DEVEM** ser explícitas, justificadas por escrito e revisadas;
  não são o caminho padrão para "fechar" a métrica.
- 100% de cobertura é piso, não teto: cobrir uma linha não substitui asserções
  significativas sobre comportamento e casos de borda.

**Racional**: Cobertura total, combinada com TDD, garante que cada caminho de execução
foi deliberadamente especificado e verificado.

### IV. Princípios SOLID

O design orientado a objetos **DEVE** seguir os cinco princípios SOLID.

- **S** — Responsabilidade Única: cada classe/módulo tem uma única razão para mudar.
- **O** — Aberto/Fechado: aberto para extensão, fechado para modificação.
- **L** — Substituição de Liskov: subtipos são substituíveis por seus tipos base sem
  quebrar contratos.
- **I** — Segregação de Interfaces: interfaces pequenas e específicas em vez de
  interfaces "gordas".
- **D** — Inversão de Dependência: depender de abstrações, não de implementações
  concretas; alinhado às portas/abstrações exigidas pelo DDD.

**Racional**: SOLID mantém o código modular, extensível e testável, reforçando os
limites de domínio do DDD e a injeção de dependências necessária ao TDD.

## Restrições de Arquitetura e Qualidade

- A dependência aponta sempre para o domínio: `infraestrutura → aplicação → domínio`.
  O domínio **NÃO DEVE** depender de camadas externas.
- Toda integração externa (DB, HTTP, filas, relógio, aleatoriedade) **DEVE** ser
  acessada por interfaces, permitindo dublês de teste e mantendo testes determinísticos.
- Código que não pode ser testado por design é considerado defeito de design e
  **DEVE** ser refatorado, não isento de cobertura.
- Ferramentas de lint/formatação e o relatório de cobertura **DEVEM** rodar na suíte
  automatizada e bloquear merge em caso de violação.

## Fluxo de Desenvolvimento e Portões de Qualidade

- Nenhuma mudança é mesclada sem: testes escritos antes da implementação (TDD),
  suíte verde, cobertura em 100% e conformidade com SOLID/DDD verificada em revisão.
- Toda Pull Request/revisão **DEVE** confirmar explicitamente a aderência aos quatro
  princípios fundamentais. Violações **DEVEM** ser corrigidas ou justificadas formalmente.
- A seção "Constitution Check" do plano de implementação deriva seus portões destes
  princípios e **DEVE** ser reavaliada após o design de cada feature.
- Complexidade adicional **DEVE** ser justificada por escrito (ver "Complexity Tracking"
  no template de plano); a simplicidade é a opção padrão (YAGNI).

## Governança

Esta constituição **prevalece** sobre quaisquer outras práticas do projeto. Em conflito
entre conveniência e princípio, o princípio vence.

- **Emendas**: qualquer alteração a esta constituição **DEVE** ser documentada,
  justificada e aprovada em revisão, com plano de migração quando afetar artefatos
  existentes (specs, planos, tasks, templates).
- **Versionamento** (Semantic Versioning):
  - **MAJOR**: remoção ou redefinição incompatível de princípios/governança.
  - **MINOR**: adição de princípio/seção ou expansão material de orientação.
  - **PATCH**: esclarecimentos, correções de redação, refinamentos não semânticos.
- **Conformidade**: toda revisão de código e todo portão de CI **DEVEM** verificar
  aderência aos princípios. Não conformidade bloqueia o merge.
- A constituição é a fonte de verdade para os portões de qualidade dos comandos
  Spec Kit (`/speckit-plan`, `/speckit-tasks`, `/speckit-implement`, `/speckit-analyze`).

**Version**: 1.0.0 | **Ratified**: 2026-05-30 | **Last Amended**: 2026-05-30
