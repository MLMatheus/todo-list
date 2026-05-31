package github.mlmatheus.todolist.infrastructure.exception;

/**
 * Lançada quando a tarefa não existe ou não pertence ao usuário autenticado.
 * Mapeada para HTTP 404 (não revela a existência de tarefas de outros usuários).
 */
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(String id) {
        super("Tarefa não encontrada: " + id);
    }
}
