package service;

import model.Department;
import model.Employee;
import repository.interfaces.IRepository;
import service.interfaces.IEmployeeService;

import java.util.List;
import java.util.Optional;

public class EmployeeService implements IEmployeeService {
    private final IRepository<Employee> employeeRepository;
    private final IRepository<Department> departmentRepository;

    public EmployeeService(IRepository<Employee> employeeRepository) {
        this(employeeRepository, null);
    }

    public EmployeeService(IRepository<Employee> employeeRepository,
                           IRepository<Department> departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Employee create(Employee entity) {
        employeeRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Employee> getById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> update(Integer id, Employee entity) {
        if (employeeRepository.findById(id).isEmpty()) {
            return Optional.empty();
        }

        entity.setId(id);

        employeeRepository.save(entity);

        return Optional.of(entity);
    }

    @Override
    public void delete(Integer id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public Optional<Employee> transferEmployeeToDepartment(int employeeId, int newDepartmentId) {
        Employee employee = employeeRepository.findById((int) employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        Department newDepartment = resolveDepartment((int) newDepartmentId);

        Department current = employee.getDepartment();
        if (current != null && current.getId() != null && current.getId() == (int) newDepartmentId) {
            throw new IllegalStateException(
                    "Employee " + employeeId + " already belongs to department " + newDepartmentId);
        }

        // Simulated transactional update: detach from current, attach to new, persist.
        if (current != null) {
            current.getEmployees().removeIf(e -> e.getId() != null && e.getId().equals(employee.getId()));
        }
        employee.setDepartment(newDepartment);
        if (!newDepartment.getEmployees().contains(employee)) {
            newDepartment.getEmployees().add(employee);
        }
        employeeRepository.save(employee);
        if (departmentRepository != null) {
            departmentRepository.save(newDepartment);
        }

        return getById(employeeId);
    }

    private Department resolveDepartment(Integer departmentId) {
        if (departmentRepository != null) {
            return departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        }
        // Dummy fallback when no department repository is wired in yet.
        Department dummy = new Department();
        dummy.setId(departmentId);
        dummy.setName("Department " + departmentId);
        return dummy;
    }
}
