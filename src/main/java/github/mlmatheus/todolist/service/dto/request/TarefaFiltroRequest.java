package github.mlmatheus.todolist.service.dto.request;

import github.mlmatheus.todolist.service.model.Status;
import java.time.LocalDate;

/** Critérios opcionais de filtragem da listagem de tarefas. */
public record TarefaFiltroRequest(
        Status status,
        Integer prioridade,
        LocalDate dataVencimento) {
}
