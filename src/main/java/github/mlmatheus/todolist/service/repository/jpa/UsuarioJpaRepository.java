package github.mlmatheus.todolist.service.repository.jpa;

import github.mlmatheus.todolist.service.repository.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, String> {

    Optional<UsuarioEntity> findByEmail(String email);
}
