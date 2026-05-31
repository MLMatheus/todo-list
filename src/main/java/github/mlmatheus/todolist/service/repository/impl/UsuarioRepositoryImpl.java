package github.mlmatheus.todolist.service.repository.impl;

import github.mlmatheus.todolist.service.mapper.TarefaPersistenceMapper;
import github.mlmatheus.todolist.service.model.Usuario;
import github.mlmatheus.todolist.service.repository.UsuarioRepository;
import github.mlmatheus.todolist.service.repository.jpa.UsuarioJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adaptador JPA da porta {@link UsuarioRepository}. */
@Component
@RequiredArgsConstructor
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;
    private final TarefaPersistenceMapper mapper;

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.toDomain(jpa.save(mapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }
}
