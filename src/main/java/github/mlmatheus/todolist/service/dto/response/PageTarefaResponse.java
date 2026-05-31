package github.mlmatheus.todolist.service.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/** Página de tarefas no formato exposto pela API. */
public record PageTarefaResponse(
        List<TarefaResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size) {

    public static PageTarefaResponse from(Page<TarefaResponse> page) {
        return new PageTarefaResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }
}
