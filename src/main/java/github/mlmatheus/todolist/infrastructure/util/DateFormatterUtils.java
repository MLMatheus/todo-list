package github.mlmatheus.todolist.infrastructure.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Formatação de datas no padrão do projeto: {@code yyyy-MM-dd HH:mm:ss.SSS}. */
public final class DateFormatterUtils {

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

    private DateFormatterUtils() {
    }

    /** Formata o instante informado; retorna {@code null} quando a entrada é {@code null}. */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return FORMATTER.format(dateTime);
    }
}
