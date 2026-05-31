package github.mlmatheus.todolist.controller.contract;

import github.mlmatheus.todolist.service.dto.request.AtualizarTarefaRequest;
import github.mlmatheus.todolist.service.dto.request.CriarTarefaRequest;
import github.mlmatheus.todolist.service.dto.response.PageTarefaResponse;
import github.mlmatheus.todolist.service.dto.response.TarefaResponse;
import github.mlmatheus.todolist.service.model.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Contrato REST do gerenciador de tarefas (documentação springdoc centralizada aqui). */
@Tag(name = "Tarefas", description = "Operações de gerenciamento de tarefas do usuário autenticado")
public interface ITodoListController {

    @Operation(summary = "Cria uma nova tarefa",
            description = "Cria uma tarefa para o usuário autenticado com status inicial PENDENTE. "
                    + "O título é obrigatório; descrição, prioridade e data de vencimento são opcionais.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @PostMapping("/tarefas")
    @ResponseStatus(HttpStatus.CREATED)
    TarefaResponse criar(@Valid @RequestBody CriarTarefaRequest request,
                         @AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Lista tarefas do usuário com filtros e paginação",
            description = "Retorna uma página das tarefas do usuário autenticado. Filtros opcionais por "
                    + "status, prioridade e data de vencimento podem ser combinados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tarefas"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping("/tarefas")
    PageTarefaResponse listar(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer prioridade,
            @RequestParam(name = "data_vencimento", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Atualiza parcialmente uma tarefa (conteúdo e/ou status)",
            description = "Atualiza parcialmente a tarefa do usuário. Campos ausentes não são alterados. "
                    + "Incluir 'status' permite concluir (CONCLUIDA) ou reabrir (PENDENTE) a tarefa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente ou de outro usuário")
    })
    @PatchMapping("/tarefas/{tarefa_id}")
    TarefaResponse atualizar(@PathVariable("tarefa_id") String tarefaId,
                             @Valid @RequestBody AtualizarTarefaRequest request,
                             @AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Exclui uma tarefa",
            description = "Remove permanentemente a tarefa do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa excluída"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente ou de outro usuário")
    })
    @DeleteMapping("/tarefas/{tarefa_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable("tarefa_id") String tarefaId,
                 @AuthenticationPrincipal Jwt jwt);
}
