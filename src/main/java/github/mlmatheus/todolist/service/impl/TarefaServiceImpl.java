package github.mlmatheus.todolist.service.impl;

import github.mlmatheus.todolist.infrastructure.exception.TarefaNaoEncontradaException;
import github.mlmatheus.todolist.service.TarefaService;
import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.TarefaFiltroRequest;
import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.repository.TarefaRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository repository;
    private final Clock clock;

    @Override
    public Tarefa criar(String usuarioId, CriarTarefaRequest request) {
        Tarefa tarefa = Tarefa.criar(
                UUID.randomUUID().toString(),
                request.titulo(),
                request.descricao(),
                request.prioridade(),
                request.dataVencimento(),
                usuarioId,
                agora());
        return repository.salvar(tarefa);
    }

    @Override
    public Page<Tarefa> listar(String usuarioId, TarefaFiltroRequest filtro, Pageable pageable) {
        return repository.listar(usuarioId, filtro.status(), filtro.prioridade(),
                filtro.dataVencimento(), pageable);
    }

    @Override
    public Tarefa atualizar(String usuarioId, String tarefaId, AtualizarTarefaRequest request) {
        Tarefa tarefa = buscar(usuarioId, tarefaId);
        if (request.status() != null) {
            aplicarStatus(tarefa, request.status());
        }
        if (temConteudo(request)) {
            tarefa.atualizarConteudo(request.titulo(), request.descricao(),
                    request.prioridade(), request.dataVencimento(), agora());
        }
        return repository.salvar(tarefa);
    }

    @Override
    public void excluir(String usuarioId, String tarefaId) {
        repository.excluir(buscar(usuarioId, tarefaId));
    }

    private Tarefa buscar(String usuarioId, String tarefaId) {
        return repository.buscarPorIdEUsuario(tarefaId, usuarioId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));
    }

    private void aplicarStatus(Tarefa tarefa, Status status) {
        if (status == Status.CONCLUIDA) {
            tarefa.concluir(agora());
        } else {
            tarefa.reabrir(agora());
        }
    }

    private boolean temConteudo(AtualizarTarefaRequest request) {
        return Stream.of(request.titulo(), request.descricao(),
                        request.prioridade(), request.dataVencimento())
                .anyMatch(Objects::nonNull);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
