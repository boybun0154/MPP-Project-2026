package repository.jdbc;

import domain.Department;
import repository.interfaces.IDepartmentRepository;

import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements IDepartmentRepository {
    @Override
    public Optional<Department> findById(int id) {
        return Optional.empty();
    }

    @Override
    public Optional<Department> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public List<Department> findAll() {
        return List.of();
    }

    @Override
    public void save(Department entity) {

    }
}
