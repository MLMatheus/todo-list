package github.mlmatheus.todolist.service.repository.spec;

import github.mlmatheus.todolist.service.model.Status;
import github.mlmatheus.todolist.service.repository.entity.TarefaEntity;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Filtros dinâmicos de tarefas via CriteriaBuilder, sempre escopados pelo usuário. */
public final class TarefaSpecification {

    private TarefaSpecification() {
    }

    public static Specification<TarefaEntity> comFiltros(String usuarioId, Status status,
                                                        Integer prioridade, LocalDate dataVencimento) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.equal(root.get("usuarioId"), usuarioId));
            if (status != null) {
                predicados.add(cb.equal(root.get("status"), status.name()));
            }
            if (prioridade != null) {
                predicados.add(cb.equal(root.get("prioridade"), prioridade));
            }
            if (dataVencimento != null) {
                predicados.add(cb.equal(root.get("dataVencimento"), dataVencimento));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
