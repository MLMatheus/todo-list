package github.mlmatheus.todolist.service.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import github.mlmatheus.todolist.infrastructure.exception.ValidacaoDominioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TarefaAtualizarConteudoTest {

    private static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 5, 30, 10, 0);
    private static final LocalDateTime EDICAO = LocalDateTime.of(2026, 6, 1, 11, 0);

    private Tarefa novaTarefa() {
        return Tarefa.criar("t1", "Original", "desc", 2, LocalDate.of(2026, 6, 10), "u1", CRIACAO);
    }

    @Test
    void atualizaTodosOsCampos() {
        Tarefa tarefa = novaTarefa();
        tarefa.atualizarConteudo("Novo", "nova desc", 1, LocalDate.of(2026, 7, 1), EDICAO);

        assertThat(tarefa.getTitulo()).isEqualTo("Novo");
        assertThat(tarefa.getDescricao()).isEqualTo("nova desc");
        assertThat(tarefa.getPrioridade()).isEqualTo(Prioridade.ALTA);
        assertThat(tarefa.getDataVencimento()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(EDICAO);
    }

    @Test
    void atualizacaoParcialMantemCamposNaoInformados() {
        Tarefa tarefa = novaTarefa();
        tarefa.atualizarConteudo("Só título", null, null, null, EDICAO);

        assertThat(tarefa.getTitulo()).isEqualTo("Só título");
        assertThat(tarefa.getDescricao()).isEqualTo("desc");
        assertThat(tarefa.getPrioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(tarefa.getDataVencimento()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(EDICAO);
    }

    @Test
    void rejeitaTituloEmBrancoNaEdicao() {
        Tarefa tarefa = novaTarefa();
        assertThatThrownBy(() -> tarefa.atualizarConteudo("  ", null, null, null, EDICAO))
                .isInstanceOf(ValidacaoDominioException.class);
    }

    @Test
    void rejeitaPrioridadeInvalidaNaEdicao() {
        Tarefa tarefa = novaTarefa();
        assertThatThrownBy(() -> tarefa.atualizarConteudo(null, null, 7, null, EDICAO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaDescricaoMuitoLongaNaEdicao() {
        Tarefa tarefa = novaTarefa();
        String longa = "x".repeat(2001);
        assertThatThrownBy(() -> tarefa.atualizarConteudo(null, longa, null, null, EDICAO))
                .isInstanceOf(ValidacaoDominioException.class);
    }
}
