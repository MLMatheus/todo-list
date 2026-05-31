package github.mlmatheus.todolist.controller;

import github.mlmatheus.todolist.controller.contract.ITodoListController;
import github.mlmatheus.todolist.infrastructure.constants.Log;
import github.mlmatheus.todolist.service.TarefaService;
import github.mlmatheus.todolist.service.UsuarioService;
import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.TarefaFiltroRequest;
import github.mlmatheus.todolist.service.dto.response.PageTarefaResponse;
import github.mlmatheus.todolist.service.dto.response.TarefaResponse;
import github.mlmatheus.todolist.service.mapper.TarefaMapper;
import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Usuario;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TodoListController implements ITodoListController {

    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;
    private final TarefaMapper mapper;

    @Override
    public TarefaResponse criar(CriarTarefaRequest request, Jwt jwt) {
        return mapper.toResponse(tarefaService.criar(usuarioId(jwt), request));
    }

    @Override
    public PageTarefaResponse listar(Status status, Integer prioridade, LocalDate dataVencimento,
                                     Pageable pageable, Jwt jwt) {
        TarefaFiltroRequest filtro = new TarefaFiltroRequest(status, prioridade, dataVencimento);
        Page<TarefaResponse> page = tarefaService.listar(usuarioId(jwt), filtro, pageable)
                .map(mapper::toResponse);
        return PageTarefaResponse.from(page);
    }

    @Override
    public TarefaResponse atualizar(String tarefaId, AtualizarTarefaRequest request, Jwt jwt) {
        return mapper.toResponse(tarefaService.atualizar(usuarioId(jwt), tarefaId, request));
    }

    @Override
    public void excluir(String tarefaId, Jwt jwt) {
        tarefaService.excluir(usuarioId(jwt), tarefaId);
    }

    private String usuarioId(Jwt jwt) {
        Usuario usuario = usuarioService.resolver(
                jwt.getSubject(), jwt.getClaimAsString("email"), jwt.getClaimAsString("name"));
        MDC.put(Log.ID_USUARIO, usuario.getId());
        return usuario.getId();
    }
}
