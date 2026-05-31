package github.mlmatheus.todolist.service;

import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.TarefaFiltroRequest;
import github.mlmatheus.todolist.service.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Casos de uso de gerenciamento de tarefas, sempre escopados pelo usuário autenticado. */
public interface TarefaService {

    Tarefa criar(String usuarioId, CriarTarefaRequest request);

    Page<Tarefa> listar(String usuarioId, TarefaFiltroRequest filtro, Pageable pageable);

    Tarefa atualizar(String usuarioId, String tarefaId, AtualizarTarefaRequest request);

    void excluir(String usuarioId, String tarefaId);
}
