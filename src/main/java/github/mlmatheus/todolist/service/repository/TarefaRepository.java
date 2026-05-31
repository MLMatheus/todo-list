package github.mlmatheus.todolist.service.repository;

import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Porta de domínio para persistência de tarefas (implementada por adaptador JPA). */
public interface TarefaRepository {

    Tarefa salvar(Tarefa tarefa);

    Optional<Tarefa> buscarPorIdEUsuario(String id, String usuarioId);

    Page<Tarefa> listar(String usuarioId, Status status, Integer prioridade,
                        LocalDate dataVencimento, Pageable pageable);

    void excluir(Tarefa tarefa);
}
