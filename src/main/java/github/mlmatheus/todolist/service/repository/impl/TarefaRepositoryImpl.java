package github.mlmatheus.todolist.service.repository.impl;

import github.mlmatheus.todolist.service.mapper.TarefaPersistenceMapper;
import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.repository.TarefaRepository;
import github.mlmatheus.todolist.service.repository.jpa.TarefaJpaRepository;
import github.mlmatheus.todolist.service.repository.spec.TarefaSpecification;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Adaptador JPA da porta {@link TarefaRepository}. */
@Component
@RequiredArgsConstructor
public class TarefaRepositoryImpl implements TarefaRepository {

    private final TarefaJpaRepository jpa;
    private final TarefaPersistenceMapper mapper;

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        return mapper.toDomain(jpa.save(mapper.toEntity(tarefa)));
    }

    @Override
    public Optional<Tarefa> buscarPorIdEUsuario(String id, String usuarioId) {
        return jpa.findByIdAndUsuarioId(id, usuarioId).map(mapper::toDomain);
    }

    @Override
    public Page<Tarefa> listar(String usuarioId, Status status, Integer prioridade,
                              LocalDate dataVencimento, Pageable pageable) {
        return jpa.findAll(
                TarefaSpecification.comFiltros(usuarioId, status, prioridade, dataVencimento), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void excluir(Tarefa tarefa) {
        jpa.deleteById(tarefa.getId());
    }
}
