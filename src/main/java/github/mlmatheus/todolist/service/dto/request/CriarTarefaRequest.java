package github.mlmatheus.todolist.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Corpo da requisição de criação de tarefa. */
public record CriarTarefaRequest(

        @Schema(description = "Título da tarefa (obrigatório)", example = "Estudar DDD")
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @Schema(description = "Descrição opcional", example = "Capítulo de agregados")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @Schema(description = "Prioridade: 1=ALTA, 2=MÉDIA, 3=BAIXA (default 2)", example = "1")
        @Min(value = 1, message = "A prioridade deve estar entre 1 e 3")
        @Max(value = 3, message = "A prioridade deve estar entre 1 e 3")
        Integer prioridade,

        @Schema(description = "Data de vencimento (yyyy-MM-dd)", example = "2026-06-10")
        @JsonProperty("data_vencimento")
        LocalDate dataVencimento) {
}
