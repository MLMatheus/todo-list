package github.mlmatheus.todolist;

import static org.assertj.core.api.Assertions.assertThat;

import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.model.Tarefa;
import github.mlmatheus.todolist.service.model.Usuario;
import github.mlmatheus.todolist.service.repository.TarefaRepository;
import github.mlmatheus.todolist.service.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/** Round-trip de persistência pela porta de domínio (cobre adaptador + mapper + JPA + migrations). */
class RepositorioTarefaIT extends AbstractIntegrationTest {

    @Autowired
    private TarefaRepository tarefaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void salvaBuscaListaEExclui() {
        Usuario usuario = usuarioRepository.salvar(Usuario.builder()
                .id(UUID.randomUUID().toString()).nome("Ana").email("ana-repo@x.com").build());

        Tarefa tarefa = Tarefa.criar(UUID.randomUUID().toString(), "Persistir", "d", 1,
                LocalDate.of(2026, 6, 10), usuario.getId(), LocalDateTime.of(2026, 5, 30, 10, 0));
        Tarefa salva = tarefaRepository.salvar(tarefa);

        assertThat(tarefaRepository.buscarPorIdEUsuario(salva.getId(), usuario.getId()))
                .isPresent()
                .get()
                .satisfies(t -> {
                    assertThat(t.getStatus()).isEqualTo(Status.PENDENTE);
                    assertThat(t.getPrioridade().codigo()).isEqualTo(1);
                });

        Page<Tarefa> page = tarefaRepository.listar(usuario.getId(), null, null, null,
                PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);

        tarefaRepository.excluir(salva);
        assertThat(tarefaRepository.buscarPorIdEUsuario(salva.getId(), usuario.getId())).isEmpty();
    }
}
