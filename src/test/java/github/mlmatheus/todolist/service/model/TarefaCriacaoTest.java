package github.mlmatheus.todolist.service.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import github.mlmatheus.todolist.infrastructure.exception.ValidacaoDominioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TarefaCriacaoTest {

    private static final LocalDateTime MOMENTO = LocalDateTime.of(2026, 5, 30, 10, 0);

    @Test
    void criaComStatusPendenteETimestamps() {
        Tarefa tarefa = Tarefa.criar("t1", "Estudar DDD", "cap 1", 1,
                LocalDate.of(2026, 6, 10), "u1", MOMENTO);

        assertThat(tarefa.getId()).isEqualTo("t1");
        assertThat(tarefa.getStatus()).isEqualTo(Status.PENDENTE);
        assertThat(tarefa.getPrioridade()).isEqualTo(Prioridade.ALTA);
        assertThat(tarefa.getDataVencimento()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(tarefa.getDataCriacao()).isEqualTo(MOMENTO);
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(MOMENTO);
        assertThat(tarefa.getUsuarioId()).isEqualTo("u1");
    }

    @Test
    void prioridadeDefaultMediaQuandoOmitida() {
        Tarefa tarefa = Tarefa.criar("t1", "Tarefa", null, null, null, "u1", MOMENTO);
        assertThat(tarefa.getPrioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(tarefa.getDescricao()).isNull();
        assertThat(tarefa.getDataVencimento()).isNull();
    }

    @Test
    void normalizaTitulo() {
        Tarefa tarefa = Tarefa.criar("t1", "  Comprar pão  ", null, 2, null, "u1", MOMENTO);
        assertThat(tarefa.getTitulo()).isEqualTo("Comprar pão");
    }

    @Test
    void rejeitaTituloNuloOuEmBranco() {
        assertThatThrownBy(() -> Tarefa.criar("t1", null, null, 2, null, "u1", MOMENTO))
                .isInstanceOf(ValidacaoDominioException.class)
                .satisfies(e -> assertThat(((ValidacaoDominioException) e).getCampo()).isEqualTo("titulo"));
        assertThatThrownBy(() -> Tarefa.criar("t1", "   ", null, 2, null, "u1", MOMENTO))
                .isInstanceOf(ValidacaoDominioException.class);
    }

    @Test
    void rejeitaTituloMuitoLongo() {
        String longo = "x".repeat(151);
        assertThatThrownBy(() -> Tarefa.criar("t1", longo, null, 2, null, "u1", MOMENTO))
                .isInstanceOf(ValidacaoDominioException.class);
    }

    @Test
    void rejeitaDescricaoMuitoLonga() {
        String longa = "x".repeat(2001);
        assertThatThrownBy(() -> Tarefa.criar("t1", "ok", longa, 2, null, "u1", MOMENTO))
                .isInstanceOf(ValidacaoDominioException.class)
                .satisfies(e -> assertThat(((ValidacaoDominioException) e).getCampo()).isEqualTo("descricao"));
    }
}
