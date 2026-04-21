package repository.interfaces;

import java.util.List;
import java.util.Optional;

public interface IRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    void save(T entity);
}