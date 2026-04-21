package repository.memory;

import repository.interfaces.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.BiConsumer;

/**
 * Lightweight in-memory IRepository implementation used until the JDBC layer
 * is finished. Keeps the service/controller wiring testable today.
 */
public class InMemoryRepository<T> implements IRepository<T> {
    private final ConcurrentHashMap<Integer, T> store = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final Function<T, Integer> idGetter;
    private final BiConsumer<T, Integer> idSetter;

    public InMemoryRepository(Function<T, Integer> idGetter, BiConsumer<T, Integer> idSetter) {
        this.idGetter = idGetter;
        this.idSetter = idSetter;
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(T entity) {
        Integer id = idGetter.apply(entity);
        if (id == null) {
            id = sequence.incrementAndGet();
            idSetter.accept(entity, id);
        } else {
            sequence.accumulateAndGet(id, Math::max);
        }
        store.put(id, entity);
    }

    @Override
    public void delete(Integer id) {

    }
}
