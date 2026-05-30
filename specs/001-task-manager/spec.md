# Feature Specification: Gerenciador de Tarefas

**Feature Branch**: `001-task-manager`

**Created**: 2026-05-30

**Status**: Draft

**Input**: User description: "Este projeto consiste num gerenciador de tarefas, onde um usuário logado poderá cadastrar, editar, excluir e concluir uma tarefa. O usuário também poderá filtrar as tarefas por data, prioridade e status."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar e visualizar tarefas (Priority: P1)

Um usuário autenticado registra uma nova tarefa informando, no mínimo, um título e,
opcionalmente, descrição, data de vencimento e prioridade. Após salvar, a tarefa
aparece na lista de tarefas do próprio usuário com status inicial "Pendente".

**Why this priority**: É o núcleo do produto e o menor incremento que já entrega valor —
sem a capacidade de cadastrar e ver tarefas, nenhuma outra funcionalidade tem sentido.
Sozinha, já constitui um MVP utilizável.

**Independent Test**: Autenticar como um usuário, cadastrar uma tarefa com título e
verificar que ela passa a ser exibida na lista do usuário com status "Pendente".

**Acceptance Scenarios**:

1. **Given** um usuário autenticado na tela de tarefas, **When** ele cadastra uma tarefa
   com título válido, **Then** a tarefa é persistida, recebe status "Pendente" e passa a
   constar na lista de tarefas do usuário.
2. **Given** um usuário autenticado, **When** ele tenta cadastrar uma tarefa sem título,
   **Then** o sistema rejeita o cadastro e informa que o título é obrigatório.
3. **Given** um usuário autenticado com tarefas cadastradas, **When** ele acessa a lista,
   **Then** vê apenas as suas próprias tarefas, e não as de outros usuários.

---

### User Story 2 - Concluir tarefa (Priority: P2)

O usuário marca uma tarefa "Pendente" como "Concluída", registrando que o trabalho foi
finalizado, e pode posteriormente reabri-la (voltar para "Pendente") caso necessário.

**Why this priority**: É o desfecho natural do ciclo de vida de uma tarefa e a ação que
mais agrega valor após o cadastro; sustenta diretamente o filtro por status.

**Independent Test**: Cadastrar uma tarefa, marcá-la como concluída e verificar que o
status muda para "Concluída"; em seguida reabri-la e verificar o retorno a "Pendente".

**Acceptance Scenarios**:

1. **Given** uma tarefa "Pendente" do usuário, **When** ele a conclui, **Then** o status
   passa para "Concluída".
2. **Given** uma tarefa "Concluída" do usuário, **When** ele a reabre, **Then** o status
   retorna para "Pendente".
3. **Given** uma tarefa já "Concluída", **When** o usuário tenta concluí-la novamente,
   **Then** o sistema mantém o status "Concluída" sem erro nem duplicidade.

---

### User Story 3 - Editar tarefa (Priority: P2)

O usuário altera os dados de uma tarefa existente — título, descrição, data de vencimento
e prioridade — e as mudanças passam a refletir na lista.

**Why this priority**: Correções e ajustes são frequentes no uso real; sem edição o
usuário precisaria excluir e recriar tarefas, degradando a experiência.

**Independent Test**: Cadastrar uma tarefa, editar seu título e prioridade e verificar que
os novos valores são exibidos e persistidos.

**Acceptance Scenarios**:

1. **Given** uma tarefa existente do usuário, **When** ele altera o título para um valor
   válido, **Then** a tarefa é atualizada e a lista reflete o novo título.
2. **Given** uma tarefa existente, **When** o usuário a edita deixando o título vazio,
   **Then** o sistema rejeita a alteração e informa que o título é obrigatório.
3. **Given** uma tarefa de outro usuário, **When** o usuário tenta editá-la, **Then** o
   sistema impede a operação.

---

### User Story 4 - Excluir tarefa (Priority: P3)

O usuário remove permanentemente uma tarefa que não é mais relevante, deixando de
exibi-la na lista.

**Why this priority**: Importante para a higiene da lista, mas menos crítica que cadastrar,
concluir e editar; o produto entrega valor mesmo sem exclusão num primeiro momento.

**Independent Test**: Cadastrar uma tarefa, excluí-la e verificar que ela deixa de aparecer
na lista do usuário.

**Acceptance Scenarios**:

1. **Given** uma tarefa existente do usuário, **When** ele a exclui e confirma a ação,
   **Then** a tarefa é removida e não aparece mais na lista.
2. **Given** uma tarefa de outro usuário, **When** o usuário tenta excluí-la, **Then** o
   sistema impede a operação.

---

### User Story 5 - Filtrar tarefas (Priority: P2)

O usuário restringe a lista exibida aplicando filtros por data de vencimento, por
prioridade e por status, isoladamente ou combinados, para encontrar tarefas rapidamente.

**Why this priority**: A partir de um volume moderado de tarefas, a busca por filtros é o
que mantém a lista navegável e útil no dia a dia.

**Independent Test**: Cadastrar tarefas com datas, prioridades e status variados, aplicar
cada filtro e verificar que apenas as tarefas correspondentes são exibidas.

**Acceptance Scenarios**:

1. **Given** tarefas com status "Pendente" e "Concluída", **When** o usuário filtra por
   status "Pendente", **Then** apenas as pendentes são exibidas.
2. **Given** tarefas com prioridades distintas, **When** o usuário filtra por prioridade
   "Alta", **Then** apenas as de prioridade "Alta" são exibidas.
3. **Given** tarefas com datas de vencimento distintas, **When** o usuário filtra por uma
   data (ou intervalo) específica, **Then** apenas as tarefas correspondentes são exibidas.
4. **Given** múltiplos filtros aplicados ao mesmo tempo, **When** o usuário combina status,
   prioridade e data, **Then** apenas as tarefas que satisfazem todos os critérios são
   exibidas.
5. **Given** filtros que não correspondem a nenhuma tarefa, **When** aplicados, **Then** o
   sistema exibe uma lista vazia com indicação clara de "nenhuma tarefa encontrada".

---

### Edge Cases

- **Título ausente ou em branco**: cadastro e edição são rejeitados com mensagem clara.
- **Título muito longo**: o sistema limita o tamanho do título e informa o usuário.
- **Data de vencimento no passado**: permitida, mas a tarefa é sinalizada como atrasada.
- **Concluir/reabrir repetidamente**: a operação é idempotente, sem efeitos colaterais.
- **Excluir tarefa inexistente** (ex.: já removida em outra sessão): o sistema responde de
  forma amigável sem quebrar a lista.
- **Filtros combinados sem resultados**: lista vazia com indicação explícita.
- **Acesso a tarefa de outro usuário**: qualquer operação (ver, editar, excluir, concluir)
  sobre tarefa que não pertence ao usuário é negada.
- **Usuário não autenticado**: nenhuma operação de tarefa é permitida; o acesso é bloqueado.
- **Lista sem nenhuma tarefa**: estado vazio orientando o usuário a cadastrar a primeira.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir que apenas usuários autenticados cadastrem, visualizem,
  editem, excluam, concluam e filtrem tarefas.
- **FR-002**: O sistema MUST associar cada tarefa ao usuário que a criou e MUST garantir que
  um usuário só acesse e manipule as suas próprias tarefas.
- **FR-003**: O sistema MUST permitir cadastrar uma tarefa com, no mínimo, um título
  obrigatório, e opcionalmente descrição, data de vencimento e prioridade.
- **FR-004**: O sistema MUST rejeitar cadastro ou edição de tarefa sem título, informando que
  o título é obrigatório.
- **FR-005**: O sistema MUST atribuir o status inicial "Pendente" a toda tarefa recém-criada.
- **FR-006**: O sistema MUST permitir que o usuário edite título, descrição, data de
  vencimento e prioridade de uma tarefa existente.
- **FR-007**: O sistema MUST permitir que o usuário exclua uma tarefa, removendo-a da sua lista.
- **FR-008**: O sistema MUST permitir que o usuário marque uma tarefa como "Concluída" e que
  reabra uma tarefa concluída, retornando-a a "Pendente".
- **FR-009**: As operações de concluir e reabrir MUST ser idempotentes (repeti-las não gera
  erro nem estado inconsistente).
- **FR-010**: O sistema MUST oferecer um conjunto definido de prioridades para a tarefa
  (Baixa, Média, Alta).
- **FR-011**: O sistema MUST representar o status da tarefa por um conjunto definido de valores
  (Pendente, Concluída).
- **FR-012**: O sistema MUST permitir filtrar a lista de tarefas por data de vencimento, por
  prioridade e por status.
- **FR-013**: O sistema MUST permitir combinar múltiplos filtros simultaneamente, retornando
  apenas as tarefas que satisfazem todos os critérios aplicados.
- **FR-014**: O sistema MUST exibir uma lista vazia com indicação clara quando nenhum item
  corresponder aos filtros ou quando o usuário não possuir tarefas.
- **FR-015**: O sistema MUST validar e limitar o tamanho do título e da descrição, informando
  o usuário quando os limites forem excedidos.
- **FR-016**: O sistema MUST negar qualquer operação sobre tarefas a usuários não autenticados.

### Key Entities *(include if feature involves data)*

- **Usuário**: representa a pessoa autenticada dona das tarefas. Atributos relevantes para
  esta feature: identificador único. É o proprietário de zero ou mais Tarefas.
- **Tarefa**: unidade de trabalho gerenciada pelo usuário. Atributos: identificador único,
  título (obrigatório), descrição (opcional), data de vencimento (opcional), prioridade
  (Baixa/Média/Alta), status (Pendente/Concluída), referência ao usuário proprietário, e
  marcação temporal de criação e de última atualização. Cada Tarefa pertence a exatamente
  um Usuário.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um usuário consegue cadastrar uma nova tarefa em menos de 30 segundos a partir
  da lista de tarefas.
- **SC-002**: 100% das tarefas exibidas a um usuário pertencem exclusivamente a esse usuário
  (nenhum vazamento de dados entre usuários).
- **SC-003**: Ao aplicar qualquer combinação de filtros, 100% dos itens exibidos satisfazem
  todos os critérios selecionados e nenhum item correspondente é omitido.
- **SC-004**: Um usuário consegue localizar uma tarefa específica usando filtros em até 3
  interações (seleções de filtro).
- **SC-005**: 95% dos usuários conseguem concluir o ciclo completo de uma tarefa (cadastrar →
  concluir) na primeira tentativa, sem ajuda externa.
- **SC-006**: Toda tentativa de acessar ou alterar tarefa de outro usuário é bloqueada em
  100% dos casos.

## Assumptions

- **Autenticação preexistente**: assume-se que o sistema já dispõe de um mecanismo de
  autenticação/identificação de usuários; esta feature consome o "usuário logado" como
  pré-condição e não cobre cadastro de conta, login ou recuperação de senha.
- **Escopo de propriedade**: cada usuário gerencia apenas as próprias tarefas; não há
  compartilhamento, atribuição a terceiros ou colaboração entre usuários nesta versão.
- **Prioridades**: o conjunto Baixa/Média/Alta é adotado como padrão razoável na ausência de
  especificação explícita.
- **Status**: o conjunto Pendente/Concluída é adotado como padrão, alinhado às ações descritas
  (cadastrar e concluir); estados intermediários (ex.: "Em andamento") ficam fora do escopo v1.
- **"Filtrar por data"**: interpretado como filtro pela data de vencimento da tarefa (data
  única ou intervalo), por ser a data mais relevante para o usuário gerenciar prazos.
- **Plataforma e notificações**: lembretes, notificações e recorrência de tarefas estão fora
  do escopo desta feature.
