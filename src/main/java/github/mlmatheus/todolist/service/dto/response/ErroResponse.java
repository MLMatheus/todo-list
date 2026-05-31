package github.mlmatheus.todolist.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Corpo padronizado das respostas de erro. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        String timestamp,
        @JsonProperty("http_status") int httpStatus,
        @JsonProperty("error_message") String errorMessage,
        Map<String, List<String>> erros) {
}
