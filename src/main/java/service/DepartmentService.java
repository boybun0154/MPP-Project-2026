package service;

import model.Department;
import model.Employee;
import repository.interfaces.IRepository;
import service.interfaces.IDepartmentService;

import java.util.List;
import java.util.Optional;

public class DepartmentService implements IDepartmentService {
    private final IRepository<Department> departmentRepository;
    private final IRepository<Employee> employeeRepository;

    public DepartmentService(IRepository<Department> departmentRepository,
                             IRepository<Employee> employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Department create(Department entity) {
        if (entity.getName() == null || entity.getName().isEmpty()) {
            throw new IllegalArgumentException("Department name is required.");
        }
        departmentRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Department> getById(Integer id) {
        return departmentRepository.findById(id);
    }

    @Override
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> update(Integer id, Department entity) {
        return departmentRepository.findById(id).map(existing -> {
            entity.setId(id);
            departmentRepository.save(entity);
            return entity;
        });
    }

    @Override
    public void delete(Integer id) {
        boolean hasEmployees = employeeRepository.findAll().stream()
                .anyMatch(emp -> emp.getDepartment() != null && emp.getDepartment().getId().equals(id));

        if (hasEmployees) {
            throw new IllegalStateException("Cannot delete department: There are employees assigned to it.");
        }

        departmentRepository.delete(id);
    }
}