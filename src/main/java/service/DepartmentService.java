package service;

import model.Department;
import repository.interfaces.IRepository;
import service.interfaces.IDepartmentService;

import java.util.List;
import java.util.Optional;

public class DepartmentService implements IDepartmentService {
    private final IRepository<Department, Long> departmentRepository;

    public DepartmentService(IRepository<Department, Long> departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department create(Department entity) {
        departmentRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Department> getById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Department update(Long id, Department entity) {
        // Placeholder: production version should validate id existence before save.
        departmentRepository.save(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        // Placeholder: IRepository currently has no delete contract.
        // Future implementation should enforce referential integrity checks.
    }
}
