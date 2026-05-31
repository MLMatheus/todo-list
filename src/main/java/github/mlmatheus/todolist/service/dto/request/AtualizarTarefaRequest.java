package github.mlmatheus.todolist.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import github.mlmatheus.todolist.service.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Corpo da atualização parcial de tarefa. Todos os campos são opcionais; informar
 * {@code status} permite concluir (CONCLUIDA) ou reabrir (PENDENTE) a tarefa.
 */
public record AtualizarTarefaRequest(

        @Schema(description = "Novo título", example = "Estudar DDD a fundo")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @Schema(description = "Nova descrição")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @Schema(description = "Nova prioridade: 1=ALTA, 2=MÉDIA, 3=BAIXA", example = "2")
        @Min(value = 1, message = "A prioridade deve estar entre 1 e 3")
        @Max(value = 3, message = "A prioridade deve estar entre 1 e 3")
        Integer prioridade,

        @Schema(description = "Nova data de vencimento (yyyy-MM-dd)")
        @JsonProperty("data_vencimento")
        LocalDate dataVencimento,

        @Schema(description = "Novo status: PENDENTE ou CONCLUIDA")
        Status status) {
}
