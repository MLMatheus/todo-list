package github.mlmatheus.todolist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import github.mlmatheus.todolist.service.impl.UsuarioServiceImpl;
import github.mlmatheus.todolist.service.model.Usuario;
import github.mlmatheus.todolist.service.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioServiceImpl service;

    @Test
    void retornaUsuarioExistenteSemProvisionar() {
        Usuario existente = Usuario.builder().id("u1").nome("Ana").email("ana@x.com").build();
        when(repository.buscarPorEmail("ana@x.com")).thenReturn(Optional.of(existente));

        Usuario resolvido = service.resolver("sub", "ana@x.com", "Ana");

        assertThat(resolvido).isSameAs(existente);
        verify(repository, never()).salvar(any());
    }

    @Test
    void provisionaNovoUsuarioQuandoNaoExiste() {
        when(repository.buscarPorEmail("novo@x.com")).thenReturn(Optional.empty());
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resolvido = service.resolver("sub", "novo@x.com", "Novo");

        assertThat(resolvido.getId()).isNotBlank();
        assertThat(resolvido.getEmail()).isEqualTo("novo@x.com");
        assertThat(resolvido.getNome()).isEqualTo("Novo");
        verify(repository).salvar(any());
    }
}
