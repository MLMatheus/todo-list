package github.mlmatheus.todolist.service.mapper;

import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.model.Usuario;
import github.mlmatheus.todolist.service.repository.entity.TarefaEntity;
import github.mlmatheus.todolist.service.repository.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapeamento entre o domínio puro e as entidades de persistência JPA. */
@Mapper(componentModel = "spring")
public interface TarefaPersistenceMapper {

    @Mapping(target = "status", expression = "java(tarefa.getStatus().name())")
    @Mapping(target = "prioridade", expression = "java(tarefa.getPrioridade().codigo())")
    TarefaEntity toEntity(Tarefa tarefa);

    @Mapping(target = "status",
            expression = "java(github.mlmatheus.todolist.service.model.Status.valueOf(entity.getStatus()))")
    @Mapping(target = "prioridade",
            expression = "java(github.mlmatheus.todolist.service.model.Prioridade.fromCodigo(entity.getPrioridade()))")
    Tarefa toDomain(TarefaEntity entity);

    UsuarioEntity toEntity(Usuario usuario);

    Usuario toDomain(UsuarioEntity entity);
}
