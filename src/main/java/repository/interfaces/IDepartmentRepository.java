package repository.interfaces;

import domain.Department;

import java.util.Optional;

public interface IDepartmentRepository {
    Optional<Department> findById(int id);
}
