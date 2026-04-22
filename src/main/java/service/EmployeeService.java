package service;

import model.Department;
import model.Employee;
import repository.interfaces.IDepartmentRepository;
import repository.interfaces.IEmployeeRepository;
import repository.jdbc.EmployeeRepository;
import repository.jdbc.core.DbClient;
import service.interfaces.IEmployeeService;

import java.util.List;
import java.util.Optional;

public class EmployeeService implements IEmployeeService {
    private final IEmployeeRepository employeeRepository;
    private final IDepartmentRepository departmentRepository;

    public EmployeeService(IEmployeeRepository employeeRepository, IDepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Employee create(Employee entity) {
        if (entity.getDepartment() == null || entity.getDepartment().getId() == null) {
            throw new IllegalArgumentException("Employee must be assigned to a department ID.");
        }

        int departmentId = entity.getDepartment().getId();
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

//        entity.setDepartment(department);

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
        return employeeRepository.findById(id).map(existing -> {
            existing.setFullName(entity.getFullName());
            existing.setTitle(entity.getTitle());
            existing.setHireDate(entity.getHireDate());
            existing.setSalary(entity.getSalary());

            employeeRepository.save(existing);

            return existing;
        });
    }

    @Override
    public void delete(Integer id) {
        employeeRepository.delete(id);
    }

    // Task 4: Employee Transfer
    @Override
    public Optional<Employee> transferEmployeeToDepartment(int employeeId, int newDepartmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        Department newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Target department not found: " + newDepartmentId));

        Integer currentDeptId = (employee.getDepartment() != null) ? employee.getDepartment().getId() : null;
        if (currentDeptId != null && currentDeptId.equals(newDepartmentId)) {
            employee.setDepartment(newDepartment);
            return Optional.of(employee);
        }

        DbClient.transaction(conn -> {
            if (employeeRepository instanceof EmployeeRepository jdbcRepo) {
                jdbcRepo.updateDepartment(conn, employeeId, newDepartmentId);
            } else {
                employee.setDepartment(newDepartment);
                employeeRepository.save(employee);
            }
            return null;
        });

        return employeeRepository.findById(employeeId);
    }
}