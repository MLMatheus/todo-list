package github.mlmatheus.todolist.service.repository.jpa;

import github.mlmatheus.todolist.service.repository.entity.TarefaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TarefaJpaRepository
        extends JpaRepository<TarefaEntity, String>, JpaSpecificationExecutor<TarefaEntity> {

    Optional<TarefaEntity> findByIdAndUsuarioId(String id, String usuarioId);
}
