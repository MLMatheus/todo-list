package github.mlmatheus.todolist.infrastructure.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DateFormatterUtilsTest {

    @Test
    void formataNoPadraoDoProjeto() {
        LocalDateTime momento = LocalDateTime.of(2026, 5, 30, 14, 3, 9, 123_000_000);
        assertThat(DateFormatterUtils.format(momento)).isEqualTo("2026-05-30 14:03:09.123");
    }

    @Test
    void retornaNullQuandoEntradaNull() {
        assertThat(DateFormatterUtils.format(null)).isNull();
    }
}
