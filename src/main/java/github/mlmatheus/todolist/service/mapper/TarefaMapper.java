package github.mlmatheus.todolist.service.mapper;

import github.mlmatheus.todolist.service.dto.response.TarefaResponse;
import github.mlmatheus.todolist.service.model.Tarefa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapeamento entre o domínio e os DTOs de resposta. */
@Mapper(componentModel = "spring")
public interface TarefaMapper {

    @Mapping(target = "status", expression = "java(tarefa.getStatus().name())")
    @Mapping(target = "prioridade", expression = "java(tarefa.getPrioridade().codigo())")
    TarefaResponse toResponse(Tarefa tarefa);
}
