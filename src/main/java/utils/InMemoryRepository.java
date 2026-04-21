package utils;

import repository.interfaces.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.BiConsumer;

/**
 * Lightweight in-memory IRepository implementation used until the JDBC layer
 * is finished. Keeps the service/controller wiring testable today.
 */
public class InMemoryRepository<T> implements IRepository<T, Long> {
    private final ConcurrentHashMap<Long, T> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final Function<T, Long> idGetter;
    private final BiConsumer<T, Long> idSetter;

    public InMemoryRepository(Function<T, Long> idGetter, BiConsumer<T, Long> idSetter) {
        this.idGetter = idGetter;
        this.idSetter = idSetter;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(T entity) {
        Long id = idGetter.apply(entity);
        if (id == null) {
            id = sequence.incrementAndGet();
            idSetter.accept(entity, id);
        } else {
            sequence.accumulateAndGet(id, Math::max);
        }
        store.put(id, entity);
    }
}
