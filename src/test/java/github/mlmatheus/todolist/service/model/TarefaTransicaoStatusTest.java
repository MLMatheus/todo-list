package github.mlmatheus.todolist.service.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TarefaTransicaoStatusTest {

    private static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 5, 30, 10, 0);
    private static final LocalDateTime DEPOIS = LocalDateTime.of(2026, 5, 31, 9, 0);

    private Tarefa novaTarefa() {
        return Tarefa.criar("t1", "Tarefa", null, 2, null, "u1", CRIACAO);
    }

    @Test
    void concluiTarefaPendente() {
        Tarefa tarefa = novaTarefa();
        tarefa.concluir(DEPOIS);
        assertThat(tarefa.getStatus()).isEqualTo(Status.CONCLUIDA);
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(DEPOIS);
    }

    @Test
    void concluirEhIdempotente() {
        Tarefa tarefa = novaTarefa();
        tarefa.concluir(DEPOIS);
        tarefa.concluir(LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(tarefa.getStatus()).isEqualTo(Status.CONCLUIDA);
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(DEPOIS);
    }

    @Test
    void reabreTarefaConcluida() {
        Tarefa tarefa = novaTarefa();
        tarefa.concluir(DEPOIS);
        tarefa.reabrir(LocalDateTime.of(2026, 6, 2, 8, 0));
        assertThat(tarefa.getStatus()).isEqualTo(Status.PENDENTE);
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(LocalDateTime.of(2026, 6, 2, 8, 0));
    }

    @Test
    void reabrirEhIdempotente() {
        Tarefa tarefa = novaTarefa();
        tarefa.reabrir(DEPOIS);
        assertThat(tarefa.getStatus()).isEqualTo(Status.PENDENTE);
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(CRIACAO);
    }
}
