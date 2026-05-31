package github.mlmatheus.todolist.service.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatusTest {

    @Test
    void valoresEConversao() {
        assertThat(Status.values()).containsExactly(Status.PENDENTE, Status.CONCLUIDA);
        assertThat(Status.valueOf("PENDENTE")).isEqualTo(Status.PENDENTE);
        assertThat(Status.valueOf("CONCLUIDA")).isEqualTo(Status.CONCLUIDA);
    }
}
