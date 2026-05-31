package github.mlmatheus.todolist.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import github.mlmatheus.todolist.infrastructure.exception.TokenInvalidoException;
import github.mlmatheus.todolist.service.TarefaService;
import github.mlmatheus.todolist.service.UsuarioService;
import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.TarefaFiltroRequest;
import github.mlmatheus.todolist.service.dto.response.PageTarefaResponse;
import github.mlmatheus.todolist.service.dto.response.TarefaResponse;
import github.mlmatheus.todolist.service.mapper.TarefaMapper;
import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.model.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class TodoListControllerTest {

    @Mock
    private TarefaService tarefaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private TarefaMapper mapper;

    @InjectMocks
    private TodoListController controller;

    private final Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("sub1")
            .claim("email", "ana@x.com")
            .claim("name", "Ana")
            .build();

    private final Usuario usuario = Usuario.builder().id("u1").nome("Ana").email("ana@x.com").build();

    private Tarefa umaTarefa() {
        return Tarefa.criar("t1", "Tarefa", null, 2, null, "u1", LocalDateTime.of(2026, 5, 30, 10, 0));
    }

    private final TarefaResponse umResponse =
            new TarefaResponse("t1", "Tarefa", null, "PENDENTE", 2, null);

    @Test
    void criaTarefa() {
        CriarTarefaRequest req = new CriarTarefaRequest("Tarefa", null, null, null);
        Tarefa tarefa = umaTarefa();
        when(usuarioService.resolver("sub1", "ana@x.com", "Ana")).thenReturn(usuario);
        when(tarefaService.criar("u1", req)).thenReturn(tarefa);
        when(mapper.toResponse(tarefa)).thenReturn(umResponse);

        assertThat(controller.criar(req, jwt)).isEqualTo(umResponse);
    }

    @Test
    void listaTarefas() {
        Pageable pageable = PageRequest.of(0, 20);
        Tarefa tarefa = umaTarefa();
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa), pageable, 1);
        when(usuarioService.resolver("sub1", "ana@x.com", "Ana")).thenReturn(usuario);
        when(tarefaService.listar(eq("u1"), any(TarefaFiltroRequest.class), eq(pageable)))
                .thenReturn(page);
        when(mapper.toResponse(tarefa)).thenReturn(umResponse);

        PageTarefaResponse resp = controller.listar(Status.PENDENTE, 2,
                LocalDate.of(2026, 6, 10), pageable, jwt);

        assertThat(resp.content()).containsExactly(umResponse);
        assertThat(resp.totalElements()).isEqualTo(1);
    }

    @Test
    void atualizaTarefa() {
        AtualizarTarefaRequest req = new AtualizarTarefaRequest(null, null, null, null, Status.CONCLUIDA);
        Tarefa tarefa = umaTarefa();
        when(usuarioService.resolver("sub1", "ana@x.com", "Ana")).thenReturn(usuario);
        when(tarefaService.atualizar("u1", "t1", req)).thenReturn(tarefa);
        when(mapper.toResponse(tarefa)).thenReturn(umResponse);

        assertThat(controller.atualizar("t1", req, jwt)).isEqualTo(umResponse);
    }

    @Test
    void excluiTarefa() {
        when(usuarioService.resolver("sub1", "ana@x.com", "Ana")).thenReturn(usuario);

        controller.excluir("t1", jwt);

        verify(tarefaService).excluir("u1", "t1");
    }

    @Test
    void rejeitaTokenSemEmailCom401() {
        Jwt semEmail = Jwt.withTokenValue("token").header("alg", "none")
                .subject("sub1").claim("name", "Ana").build();
        CriarTarefaRequest req = new CriarTarefaRequest("Tarefa", null, null, null);

        assertThatThrownBy(() -> controller.criar(req, semEmail))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void usaEmailComoNomeQuandoNameAusente() {
        Jwt semNome = Jwt.withTokenValue("token").header("alg", "none")
                .subject("sub1").claim("email", "ana@x.com").build();
        CriarTarefaRequest req = new CriarTarefaRequest("Tarefa", null, null, null);
        Tarefa tarefa = umaTarefa();
        when(usuarioService.resolver("sub1", "ana@x.com", "ana@x.com")).thenReturn(usuario);
        when(tarefaService.criar("u1", req)).thenReturn(tarefa);
        when(mapper.toResponse(tarefa)).thenReturn(umResponse);

        assertThat(controller.criar(req, semNome)).isEqualTo(umResponse);
        verify(usuarioService).resolver("sub1", "ana@x.com", "ana@x.com");
    }
}
