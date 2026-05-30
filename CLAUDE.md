<!-- SPECKIT START -->
## Active Feature: 001-task-manager (Gerenciador de Tarefas)

Read the current plan and design artifacts before coding:
- Plan: `specs/001-task-manager/plan.md`
- Spec: `specs/001-task-manager/spec.md`
- Research: `specs/001-task-manager/research.md`
- Data model: `specs/001-task-manager/data-model.md`
- API contract: `specs/001-task-manager/contracts/openapi.yaml`
- Quickstart: `specs/001-task-manager/quickstart.md`

**Stack**: Java 21, Spring Boot 3.5, MySQL + Flyway, JPA Criteria/Specifications, Spring
Security OAuth2 Resource Server (Google JWT), springdoc, structured JSON logs, Docker
(multi-stage) + docker-compose (profile `mysql`), Maven (profile `integration`), Testcontainers.

**Constitution (NÃO NEGOCIÁVEL)**: DDD, TDD (Red-Green-Refactor), 100% cobertura (JaCoCo gate),
SOLID. Base URL `/todo-list/v1`. Package root `github.mlmatheus.todolist`.
<!-- SPECKIT END -->
