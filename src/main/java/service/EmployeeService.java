package service;

import model.Employee;
import repository.interfaces.IRepository;
import service.interfaces.IEmployeeService;

import java.util.List;
import java.util.Optional;

public class EmployeeService implements IEmployeeService {
    private final IRepository<Employee, Long> employeeRepository;

    public EmployeeService(IRepository<Employee, Long> employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(Employee entity) {
        employeeRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Employee> getById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee update(Long id, Employee entity) {
        // Placeholder: production version should validate id existence before save.
        employeeRepository.save(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public void transferEmployeeToDepartment(int employeeId, int newDepartmentId) {
        // Placeholder for transactional logic:
        // 1) validate employee and department exist
        // 2) validate transfer constraints
        // 3) update employee department in a transaction
        throw new UnsupportedOperationException("transferEmployeeToDepartment is not implemented yet.");
    }
}
