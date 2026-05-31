package github.mlmatheus.todolist.service.impl;

import github.mlmatheus.todolist.service.UsuarioService;
import github.mlmatheus.todolist.service.model.Usuario;
import github.mlmatheus.todolist.service.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;

    @Override
    public Usuario resolver(String sub, String email, String nome) {
        return repository.buscarPorEmail(email)
                .orElseGet(() -> repository.salvar(Usuario.builder()
                        .id(UUID.randomUUID().toString())
                        .nome(nome)
                        .email(email)
                        .build()));
    }
}
