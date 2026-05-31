package github.mlmatheus.todolist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import github.mlmatheus.todolist.infrastructure.exception.TarefaNaoEncontradaException;
import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.TarefaFiltroRequest;
import github.mlmatheus.todolist.service.impl.TarefaServiceImpl;
import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.repository.TarefaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-05-30T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AGORA = LocalDateTime.now(CLOCK);
    private static final String USER = "u1";

    @Mock
    private TarefaRepository repository;

    private TarefaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TarefaServiceImpl(repository, CLOCK);
    }

    private Tarefa tarefaPendente() {
        return Tarefa.criar("t1", "Tarefa", "desc", 2, LocalDate.of(2026, 6, 10), USER,
                LocalDateTime.of(2026, 5, 29, 8, 0));
    }

    @Test
    void criaTarefaComStatusPendente() {
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        CriarTarefaRequest req = new CriarTarefaRequest("Nova", null, null, null);

        Tarefa criada = service.criar(USER, req);

        assertThat(criada.getStatus()).isEqualTo(Status.PENDENTE);
        assertThat(criada.getUsuarioId()).isEqualTo(USER);
        assertThat(criada.getDataCriacao()).isEqualTo(AGORA);
        verify(repository).salvar(any());
    }

    @Test
    void listaDelegaParaRepositorioComFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Tarefa> page = new PageImpl<>(java.util.List.of(tarefaPendente()));
        TarefaFiltroRequest filtro = new TarefaFiltroRequest(Status.PENDENTE, 1, LocalDate.of(2026, 6, 10));
        when(repository.listar(USER, Status.PENDENTE, 1, LocalDate.of(2026, 6, 10), pageable))
                .thenReturn(page);

        assertThat(service.listar(USER, filtro, pageable)).isSameAs(page);
    }

    @Test
    void atualizaSomenteStatusConcluindo() {
        Tarefa tarefa = tarefaPendente();
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarefa atualizada = service.atualizar(USER, "t1",
                new AtualizarTarefaRequest(null, null, null, null, Status.CONCLUIDA));

        assertThat(atualizada.getStatus()).isEqualTo(Status.CONCLUIDA);
        assertThat(atualizada.getDataAtualizacao()).isEqualTo(AGORA);
    }

    @Test
    void atualizaStatusReabrindo() {
        Tarefa tarefa = tarefaPendente();
        tarefa.concluir(LocalDateTime.of(2026, 5, 29, 9, 0));
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarefa atualizada = service.atualizar(USER, "t1",
                new AtualizarTarefaRequest(null, null, null, null, Status.PENDENTE));

        assertThat(atualizada.getStatus()).isEqualTo(Status.PENDENTE);
    }

    @Test
    void atualizaSomenteConteudo() {
        Tarefa tarefa = tarefaPendente();
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarefa atualizada = service.atualizar(USER, "t1",
                new AtualizarTarefaRequest("Novo título", null, 1, null, null));

        assertThat(atualizada.getTitulo()).isEqualTo("Novo título");
        assertThat(atualizada.getStatus()).isEqualTo(Status.PENDENTE);
    }

    @Test
    void atualizaStatusEConteudoJuntos() {
        Tarefa tarefa = tarefaPendente();
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarefa atualizada = service.atualizar(USER, "t1",
                new AtualizarTarefaRequest("X", "y", 3, LocalDate.of(2026, 7, 1), Status.CONCLUIDA));

        assertThat(atualizada.getStatus()).isEqualTo(Status.CONCLUIDA);
        assertThat(atualizada.getTitulo()).isEqualTo("X");
    }

    @Test
    void atualizaSemCamposEhNoOpMasSalva() {
        Tarefa tarefa = tarefaPendente();
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarefa atualizada = service.atualizar(USER, "t1",
                new AtualizarTarefaRequest(null, null, null, null, null));

        assertThat(atualizada.getStatus()).isEqualTo(Status.PENDENTE);
        verify(repository).salvar(tarefa);
    }

    @Test
    void atualizaLancaQuandoNaoEncontrada() {
        when(repository.buscarPorIdEUsuario("x", USER)).thenReturn(Optional.empty());
        AtualizarTarefaRequest req = new AtualizarTarefaRequest(null, null, null, null, Status.CONCLUIDA);

        assertThatThrownBy(() -> service.atualizar(USER, "x", req))
                .isInstanceOf(TarefaNaoEncontradaException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void excluiTarefaDoUsuario() {
        Tarefa tarefa = tarefaPendente();
        when(repository.buscarPorIdEUsuario("t1", USER)).thenReturn(Optional.of(tarefa));

        service.excluir(USER, "t1");

        verify(repository).excluir(tarefa);
    }

    @Test
    void excluiLancaQuandoNaoEncontrada() {
        when(repository.buscarPorIdEUsuario(eq("x"), eq(USER))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(USER, "x"))
                .isInstanceOf(TarefaNaoEncontradaException.class);
        verify(repository, never()).excluir(any());
    }
}
