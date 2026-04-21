package repository.interfaces;

import model.Department;

import java.util.Optional;

public interface IEmployeeRepository extends IRepository<Department, Integer> {
    Optional<Department> findById(int id);
}
