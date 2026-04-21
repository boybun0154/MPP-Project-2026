package service;

import model.Department;
import model.Employee;
import repository.interfaces.IRepository;
import service.interfaces.IEmployeeService;

import java.util.List;
import java.util.Optional;

public class EmployeeService implements IEmployeeService {
    private final IRepository<Employee, Long> employeeRepository;
    private final IRepository<Department, Long> departmentRepository;

    public EmployeeService(IRepository<Employee, Long> employeeRepository) {
        this(employeeRepository, null);
    }

    public EmployeeService(IRepository<Employee, Long> employeeRepository,
                           IRepository<Department, Long> departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
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
        Employee employee = employeeRepository.findById((long) employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        Department newDepartment = resolveDepartment((long) newDepartmentId);

        Department current = employee.getDepartment();
        if (current != null && current.getId() != null && current.getId() == (long) newDepartmentId) {
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
    }

    private Department resolveDepartment(Long departmentId) {
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
