package github.mlmatheus.todolist.service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** Representação de uma tarefa nas respostas da API. */
public record TarefaResponse(
        String id,
        String titulo,
        String descricao,
        String status,
        int prioridade,
        @JsonProperty("data_vencimento") LocalDate dataVencimento) {
}
