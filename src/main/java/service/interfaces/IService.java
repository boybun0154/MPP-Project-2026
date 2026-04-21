package service.interfaces;

import java.util.List;
import java.util.Optional;

public interface IService<T> {
    T create(T entity);
    Optional<T> getById(Integer id);
    List<T> getAll();
    Optional<T> update(Integer id, T entity);
    void delete(Integer id);
}
