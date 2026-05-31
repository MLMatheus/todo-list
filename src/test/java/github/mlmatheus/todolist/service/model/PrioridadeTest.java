package github.mlmatheus.todolist.service.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PrioridadeTest {

    @Test
    void mapeiaCodigos() {
        assertThat(Prioridade.fromCodigo(1)).isEqualTo(Prioridade.ALTA);
        assertThat(Prioridade.fromCodigo(2)).isEqualTo(Prioridade.MEDIA);
        assertThat(Prioridade.fromCodigo(3)).isEqualTo(Prioridade.BAIXA);
    }

    @Test
    void exponeCodigo() {
        assertThat(Prioridade.ALTA.codigo()).isEqualTo(1);
        assertThat(Prioridade.MEDIA.codigo()).isEqualTo(2);
        assertThat(Prioridade.BAIXA.codigo()).isEqualTo(3);
    }

    @Test
    void rejeitaCodigoInvalido() {
        assertThatThrownBy(() -> Prioridade.fromCodigo(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Prioridade.fromCodigo(9))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultMediaQuandoNull() {
        assertThat(Prioridade.fromCodigoOrDefault(null)).isEqualTo(Prioridade.MEDIA);
        assertThat(Prioridade.fromCodigoOrDefault(1)).isEqualTo(Prioridade.ALTA);
    }

    @Test
    void valueOfEValues() {
        assertThat(Prioridade.valueOf("BAIXA")).isEqualTo(Prioridade.BAIXA);
        assertThat(Prioridade.values()).containsExactly(
                Prioridade.ALTA, Prioridade.MEDIA, Prioridade.BAIXA);
    }
}
