package repository.interfaces;

import java.util.List;
import java.util.Optional;

public interface IRepository<T> {
    Optional<T> findById(int id);

    List<T> findAll();

    void save(T entity);
}